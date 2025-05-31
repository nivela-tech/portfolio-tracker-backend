package com.portfolio.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller to handle frontend routes for Single Page Application (SPA).
 * This controller ensures that React Router routes are properly handled by
 * serving the index.html file for all non-API requests.
 */
@Controller
public class SpaController {    /**
     * Forward all non-API routes to index.html to support React Router.
     * This ensures that when users navigate directly to routes like /portfolio, /performance, etc.,
     * or refresh the page on these routes, the React application is served instead of a 404 error.
     * 
     * Note: This mimics the behavior of React's development server (historyApiFallback)
     * which automatically works in local development but needs to be configured in production.
     */
    @RequestMapping(
        value = {
            "/", 
            "/portfolio", 
            "/portfolio/**", 
            "/performance", 
            "/performance/**",
            "/settings", 
            "/settings/**",
            "/accounts", 
            "/accounts/**",
            "/add-entry/**"
        },
        method = {RequestMethod.GET}
    )
    public String forward() {
        return "forward:/index.html";
    }
}
