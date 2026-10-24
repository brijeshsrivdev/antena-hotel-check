import type { NextConfig } from "next";

/**
 * The page calls the backend through a same-origin rewrite, so the Spring app needs no CORS rule.
 * HOTEL_CHECK_API_URL is the backend origin on the developer's machine; it is read at build/start time.
 */
const api = process.env.HOTEL_CHECK_API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  async rewrites() {
    return [{ source: "/api/site-checks/:path*", destination: `${api}/api/site-checks/:path*` }, { source: "/api/site-checks", destination: `${api}/api/site-checks` }];
  },
};

export default nextConfig;
