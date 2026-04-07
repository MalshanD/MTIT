package com.onlinelearning.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Serves an aggregated Swagger UI for the API Gateway.
 * Includes a built-in token manager so users can login once
 * and all subsequent API calls include the Bearer token.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public RouterFunction<ServerResponse> swaggerRoutes() {
        return RouterFunctions.route()
                .GET("/swagger-ui.html", request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(getSwaggerHtml()))
                .GET("/swagger-ui", request ->
                        ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(getSwaggerHtml()))
                .GET("/", request ->
                        ServerResponse.temporaryRedirect(
                                java.net.URI.create("/swagger-ui.html"))
                                .build())
                .build();
    }

    private String getSwaggerHtml() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Online Learning Platform - API Gateway</title>
                    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.11.0/swagger-ui.min.css">
                    <style>
                        * { box-sizing: border-box; }
                        body { margin: 0; background: #fafafa; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }

                        .gateway-header {
                            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
                            color: white;
                            padding: 20px 40px;
                        }
                        .gateway-header h1 { margin: 0 0 6px 0; font-size: 22px; font-weight: 600; }
                        .gateway-header p { margin: 0; opacity: 0.85; font-size: 13px; }
                        .badge {
                            display: inline-block;
                            background: rgba(255,255,255,0.15);
                            padding: 3px 10px;
                            border-radius: 20px;
                            font-size: 11px;
                            margin-top: 8px;
                            margin-right: 6px;
                        }

                        .controls-bar {
                            background: #fff;
                            border-bottom: 1px solid #e0e0e0;
                            padding: 14px 40px;
                            display: flex;
                            align-items: center;
                            gap: 20px;
                            flex-wrap: wrap;
                        }
                        .controls-bar label { font-weight: 600; font-size: 13px; white-space: nowrap; }
                        .controls-bar select, .controls-bar input[type="text"] {
                            padding: 8px 12px;
                            font-size: 13px;
                            border: 2px solid #ccc;
                            border-radius: 6px;
                            background: white;
                        }
                        .controls-bar select:focus, .controls-bar input:focus { outline: none; border-color: #0f3460; }
                        .controls-bar select { min-width: 220px; cursor: pointer; }
                        .controls-bar input[type="text"] { flex: 1; min-width: 200px; }

                        .token-section { display: flex; align-items: center; gap: 8px; flex: 1; }

                        .btn {
                            padding: 8px 16px;
                            font-size: 13px;
                            font-weight: 600;
                            border: none;
                            border-radius: 6px;
                            cursor: pointer;
                            white-space: nowrap;
                        }
                        .btn-login { background: #49cc90; color: white; }
                        .btn-login:hover { background: #3db87e; }
                        .btn-clear { background: #f93e3e; color: white; }
                        .btn-clear:hover { background: #e02020; }

                        .token-status {
                            font-size: 12px;
                            padding: 4px 10px;
                            border-radius: 12px;
                            font-weight: 600;
                            white-space: nowrap;
                        }
                        .token-status.active { background: #d4edda; color: #155724; }
                        .token-status.inactive { background: #f8d7da; color: #721c24; }

                        .info-bar {
                            background: #e8f4fd;
                            border-left: 4px solid #0f3460;
                            padding: 10px 40px;
                            font-size: 12px;
                            color: #333;
                        }
                        .info-bar code {
                            background: #d4e8f7;
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-size: 11px;
                        }

                        /* Login modal */
                        .modal-overlay {
                            display: none;
                            position: fixed;
                            top: 0; left: 0; right: 0; bottom: 0;
                            background: rgba(0,0,0,0.5);
                            z-index: 9999;
                            justify-content: center;
                            align-items: center;
                        }
                        .modal-overlay.show { display: flex; }
                        .modal {
                            background: white;
                            border-radius: 12px;
                            padding: 30px;
                            width: 420px;
                            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
                        }
                        .modal h2 { margin: 0 0 6px 0; font-size: 20px; }
                        .modal p { margin: 0 0 20px 0; color: #666; font-size: 13px; }
                        .modal .form-group { margin-bottom: 14px; }
                        .modal .form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 4px; }
                        .modal .form-group input {
                            width: 100%;
                            padding: 10px;
                            font-size: 14px;
                            border: 2px solid #ddd;
                            border-radius: 6px;
                        }
                        .modal .form-group input:focus { outline: none; border-color: #0f3460; }
                        .modal-buttons { display: flex; gap: 10px; margin-top: 20px; }
                        .modal-buttons .btn { flex: 1; padding: 10px; font-size: 14px; }
                        .btn-cancel { background: #e0e0e0; color: #333; }
                        .modal .error { color: #f93e3e; font-size: 13px; margin-top: 8px; display: none; }
                        .modal .hint { background: #f8f9fa; border-radius: 6px; padding: 10px; font-size: 12px; color: #555; margin-bottom: 15px; }
                        .modal .hint b { color: #333; }

                        #swagger-ui .topbar { display: none; }
                    </style>
                </head>
                <body>
                    <div class="gateway-header">
                        <h1>Online Learning Platform - API Gateway</h1>
                        <p>Centralized entry point for all microservices &bull; All requests route through port 8080</p>
                        <span class="badge">Spring Cloud Gateway (Reactive)</span>
                        <span class="badge">OAuth 2.0 Opaque Token</span>
                        <span class="badge">7 Microservices</span>
                    </div>

                    <div class="controls-bar">
                        <div>
                            <label>Service:</label>
                            <select id="serviceSelect" onchange="loadService()">
                                <option value="/services/auth/v3/api-docs">Auth Service (8081)</option>
                                <option value="/services/student/v3/api-docs">Student Service (8082)</option>
                                <option value="/services/course/v3/api-docs">Course Service (8083)</option>
                                <option value="/services/instructor/v3/api-docs">Instructor Service (8084)</option>
                                <option value="/services/enrollment/v3/api-docs">Enrollment Service (8085)</option>
                                <option value="/services/quiz/v3/api-docs">Quiz Service (8086)</option>
                                <option value="/services/certificate/v3/api-docs">Certificate Service (8087)</option>
                            </select>
                        </div>

                        <div class="token-section">
                            <label>Token:</label>
                            <input type="text" id="tokenInput" placeholder="Click 'Quick Login' or paste Bearer token here..." />
                            <button class="btn btn-login" onclick="showLoginModal()">Quick Login</button>
                            <button class="btn btn-clear" onclick="clearToken()">Clear</button>
                            <span id="tokenStatus" class="token-status inactive">No Token</span>
                        </div>
                    </div>

                    <div class="info-bar">
                        <strong>Flow:</strong>
                        Client &rarr; <code>localhost:8080</code> (Gateway)
                        &rarr; AuthenticationFilter validates Bearer token
                        &rarr; Adds <code>X-User-Id</code>, <code>X-Username</code>, <code>X-User-Role</code> headers
                        &rarr; Routes to microservice
                        &rarr; Response back through Gateway
                    </div>

                    <div id="swagger-ui"></div>

                    <!-- Login Modal -->
                    <div id="loginModal" class="modal-overlay">
                        <div class="modal">
                            <h2>Login to Get Token</h2>
                            <p>Authenticate via Auth Service to get a Bearer token for API calls.</p>
                            <div class="hint">
                                <b>Seeded accounts:</b> admin / instructor1-50 / student1-500<br>
                                <b>Password for all:</b> password123
                            </div>
                            <div class="form-group">
                                <label>Username</label>
                                <input type="text" id="loginUsername" value="admin" />
                            </div>
                            <div class="form-group">
                                <label>Password</label>
                                <input type="password" id="loginPassword" value="password123" />
                            </div>
                            <div id="loginError" class="error"></div>
                            <div class="modal-buttons">
                                <button class="btn btn-cancel" onclick="hideLoginModal()">Cancel</button>
                                <button class="btn btn-login" onclick="doLogin()" id="loginBtn">Login & Set Token</button>
                            </div>
                        </div>
                    </div>

                    <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.11.0/swagger-ui-bundle.min.js"></script>
                    <script src="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/5.11.0/swagger-ui-standalone-preset.min.js"></script>
                    <script>
                        let currentToken = '';

                        function getToken() {
                            return document.getElementById('tokenInput').value.trim();
                        }

                        function updateTokenStatus() {
                            const token = getToken();
                            const status = document.getElementById('tokenStatus');
                            if (token) {
                                status.textContent = 'Token Set';
                                status.className = 'token-status active';
                                currentToken = token;
                            } else {
                                status.textContent = 'No Token';
                                status.className = 'token-status inactive';
                                currentToken = '';
                            }
                        }

                        function clearToken() {
                            document.getElementById('tokenInput').value = '';
                            updateTokenStatus();
                        }

                        // Listen for manual token input
                        document.getElementById('tokenInput').addEventListener('input', updateTokenStatus);

                        function showLoginModal() {
                            document.getElementById('loginModal').classList.add('show');
                            document.getElementById('loginError').style.display = 'none';
                        }

                        function hideLoginModal() {
                            document.getElementById('loginModal').classList.remove('show');
                        }

                        async function doLogin() {
                            const username = document.getElementById('loginUsername').value;
                            const password = document.getElementById('loginPassword').value;
                            const errorEl = document.getElementById('loginError');
                            const btn = document.getElementById('loginBtn');

                            btn.textContent = 'Logging in...';
                            btn.disabled = true;
                            errorEl.style.display = 'none';

                            try {
                                const response = await fetch('http://localhost:8080/api/auth/login', {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ username, password })
                                });

                                const data = await response.json();

                                if (response.ok && data.accessToken) {
                                    document.getElementById('tokenInput').value = 'Bearer ' + data.accessToken;
                                    updateTokenStatus();
                                    hideLoginModal();
                                } else {
                                    errorEl.textContent = data.error || data.message || 'Login failed. Check credentials.';
                                    errorEl.style.display = 'block';
                                }
                            } catch (err) {
                                errorEl.textContent = 'Cannot reach Auth Service. Is it running on port 8081?';
                                errorEl.style.display = 'block';
                            }

                            btn.textContent = 'Login & Set Token';
                            btn.disabled = false;
                        }

                        function initSwagger(url) {
                            SwaggerUIBundle({
                                url: url,
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                plugins: [
                                    SwaggerUIBundle.plugins.DownloadUrl
                                ],
                                layout: "StandaloneLayout",
                                defaultModelsExpandDepth: -1,
                                docExpansion: "list",
                                filter: true,
                                tryItOutEnabled: true,
                                requestInterceptor: function(req) {
                                    const token = getToken();
                                    if (token) {
                                        // Add Bearer token to every request
                                        if (token.startsWith('Bearer ')) {
                                            req.headers['Authorization'] = token;
                                        } else {
                                            req.headers['Authorization'] = 'Bearer ' + token;
                                        }
                                    }
                                    return req;
                                }
                            });
                        }

                        function loadService() {
                            const url = document.getElementById('serviceSelect').value;
                            initSwagger(url);
                        }

                        // Initial load
                        window.onload = function() {
                            initSwagger('/services/auth/v3/api-docs');
                        };
                    </script>
                </body>
                </html>
                """;
    }
}
