import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { siteCheckEnabled } from "@/lib/siteCheck";
import { SiteCheckTool } from "./SiteCheckTool";

export const metadata: Metadata = {
  title: "Hotel Site Check — what stops your hotel website ranking in Google",
  robots: { index: false, follow: false },
};

/** Local-only: a 404 unless NEXT_PUBLIC_SITECHECK_ENABLED=true in frontend/.env.local (REQ-040). */
export default function HotelSiteCheckPage() {
  if (!siteCheckEnabled()) {
    notFound();
  }
  return (
    <main>
      <h1>Hotel Site Check</h1>
      <p className="lead">
        Enter a hotel website. We read up to five of its pages, robots.txt, the sitemap and Google&apos;s PageSpeed data, and list what stops it
        ranking better in Google, each with what we saw and the one change that fixes it.
      </p>
      <SiteCheckTool />
    </main>
  );
}
