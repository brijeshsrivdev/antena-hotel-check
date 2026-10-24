/** Typed contract of the backend's /api/site-checks (SiteCheckResponse in com.antenapro.hotelcheck.sitecheck.api). */

export type CheckStatus = "PASS" | "PARTIAL" | "FAIL" | "UNKNOWN";
export type RunStatus = "RUNNING" | "DONE" | "FAILED";
export type Stage = "DISCOVER" | "UNDERSTAND" | "EXPLORE" | "TRUST" | "BOOK";

export interface StageScore {
  stage: Stage;
  label: string;
  weight: number;
  score: number | null;
  pass: number;
  partial: number;
  fail: number;
  unknown: number;
}

export interface Finding {
  check: string;
  title: string;
  stage: Stage;
  status: CheckStatus;
  evidence: string;
  fix: string;
  why: string;
  antenaHandles: boolean;
}

export interface Fetched {
  url: string;
  finalUrl: string;
  role: string;
  status: number;
  ms: number;
  bytes: number;
  error: string | null;
}

export interface SiteCheckResponse {
  runId: string;
  status: RunStatus;
  requestedUrl: string;
  finalUrl: string | null;
  score: number | null;
  stages: StageScore[];
  findings: Finding[];
  fetched: Fetched[];
  limitations: string[];
  failure: string | null;
  startedAt: string;
  finishedAt: string | null;
}

export class SiteCheckError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

/** The page is served only when this is "true" in frontend/.env.local; the build never sets it. */
export const siteCheckEnabled = (): boolean => process.env.NEXT_PUBLIC_SITECHECK_ENABLED === "true";

export async function startSiteCheck(url: string): Promise<string> {
  const res = await fetch("/api/site-checks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });
  if (res.status === 202) {
    const body = (await res.json()) as { runId: string };
    return body.runId;
  }
  const body = await res.json().catch(() => ({}));
  const message = typeof body.error === "string" ? body.error : `The check could not be started (HTTP ${res.status}).`;
  throw new SiteCheckError(message, res.status);
}

export async function getSiteCheck(runId: string): Promise<SiteCheckResponse> {
  const res = await fetch(`/api/site-checks/${encodeURIComponent(runId)}`, { cache: "no-store" });
  if (!res.ok) {
    throw new SiteCheckError(res.status === 404 ? "This run is no longer available; start a new check." : `HTTP ${res.status}`, res.status);
  }
  return (await res.json()) as SiteCheckResponse;
}

/** Polls every two seconds (plan §4) until the run is DONE or FAILED. */
export async function waitForSiteCheck(runId: string, onUpdate: (r: SiteCheckResponse) => void, signal?: AbortSignal): Promise<SiteCheckResponse> {
  for (;;) {
    const r = await getSiteCheck(runId);
    onUpdate(r);
    if (r.status !== "RUNNING" || signal?.aborted) {
      return r;
    }
    await new Promise((resolve) => setTimeout(resolve, 2000));
  }
}

export const STATUS_ORDER: CheckStatus[] = ["FAIL", "PARTIAL", "UNKNOWN", "PASS"];
export const STATUS_LABEL: Record<CheckStatus, string> = { PASS: "Pass", PARTIAL: "Partly", FAIL: "Fix this", UNKNOWN: "Unknown" };
