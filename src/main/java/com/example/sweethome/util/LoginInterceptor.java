package com.example.sweethome.util;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {

	private boolean isAjax(HttpServletRequest req) {
		String xr = req.getHeader("X-Requested-With");
		return xr != null && "XMLHttpRequest".equalsIgnoreCase(xr);
	}

	private boolean acceptsHtml(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		return accept != null && accept.contains("text/html");
	}

	private boolean isDocumentRequest(HttpServletRequest req) {
		// 크롬의 fetch dest가 문서가 아닌 경우(이미지/스크립트/empty 등) 배제
		String dest = req.getHeader("Sec-Fetch-Dest");
		return dest == null || "document".equalsIgnoreCase(dest);
	}

	private boolean isPrevPageCandidate(String uri) {
		// prevPage로 쓰면 안 되는 경로들 필터링
		return !(uri.startsWith("/.well-known") || uri.startsWith("/css/") || uri.startsWith("/js/")
				|| uri.startsWith("/img/") || uri.startsWith("/images/") || uri.equals("/favicon.ico")
				|| uri.startsWith("/error") || uri.startsWith("/user/")); // 로그인/회원/비번관련 자체도 제외
	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
		Object userProfile = req.getSession().getAttribute("userProfile");
		if (userProfile != null)
			return true;

		// 미로그인: prevPage 저장 조건을 엄격히
		String uri = req.getRequestURI();
		String qs = req.getQueryString();
		String fullUrl = uri + (qs != null ? "?" + qs : "");

		if ("GET".equalsIgnoreCase(req.getMethod()) && acceptsHtml(req) && isDocumentRequest(req) && !isAjax(req)
				&& isPrevPageCandidate(uri)) {

			// 이미 값이 있으면 덮어쓰지 않음 (최초 의도 유지)
			if (req.getSession().getAttribute("prevPage") == null) {
				req.getSession().setAttribute("prevPage", fullUrl);
			}
		}

		res.sendRedirect("/user/login");
		return false;
	}

	/*
	 * @Override public boolean preHandle(HttpServletRequest request,
	 * HttpServletResponse response, Object handler) throws Exception { // 세션에서 로그인
	 * 여부 확인 Object userProfile = request.getSession().getAttribute("userProfile");
	 * 
	 * // 로그인하지 않은 경우 if (userProfile == null) { String currentRequestUri =
	 * request.getRequestURI(); String queryString = request.getQueryString();
	 * String fullUrl = currentRequestUri + (queryString != null ? "?" + queryString
	 * : "");
	 * 
	 * // prevPage가 없을 경우 기본값 설정 (예: 홈 페이지로 리다이렉트) if (fullUrl == null ||
	 * fullUrl.isEmpty()) { fullUrl = "/"; // 기본 URL을 설정 }
	 * 
	 * // 세션에 prevPage를 저장 request.getSession().setAttribute("prevPage", fullUrl);
	 * 
	 * HttpSession session = request.getSession();
	 * System.out.println("=== 세션 값 확인 ===");
	 * session.getAttributeNames().asIterator().forEachRemaining(name -> {
	 * System.out.println(name + " : " + session.getAttribute(name)); });
	 * System.out.println("==================");
	 * 
	 * 
	 * // 로그인 페이지로 리다이렉트 response.sendRedirect("/user/login"); return false; }
	 * return true; // 로그인된 경우 요청을 계속 처리 }
	 */
}
