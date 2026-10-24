import Link from "next/link";
import { siteCheckEnabled } from "@/lib/siteCheck";

/** Owner-facing home. The Hotel Site Check card appears only on a machine where the local-only flag is set. */
export default function HomePage() {
  return (
    <main>
      <h1>Antena Hotel Check</h1>
      <p className="lead">Can a guest find, understand, trust, explore and book this hotel online?</p>
      <div className="hub">
        {siteCheckEnabled() ? (
          <Link className="card" href="/tools/hotel-site-check">
            <strong>Hotel Site Check</strong>
            <p className="muted" style={{ margin: "6px 0 0" }}>What stops a hotel website ranking better in Google: 27 checks with evidence and a fix for each.</p>
          </Link>
        ) : (
          <p className="muted">No tools are enabled in this build.</p>
        )}
      </div>
    </main>
  );
}
