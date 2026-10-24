import { STATUS_LABEL, type Finding } from "@/lib/siteCheck";

/** A finding: status, title, the one-sentence fix, why it matters, and the evidence behind a toggle. */
export function FindingItem({ finding }: { finding: Finding }) {
  return (
    <li className="finding">
      <header>
        <span className={`status ${finding.status}`}>{STATUS_LABEL[finding.status]}</span>
        <strong>{finding.title}</strong>
        <span className="stage-tag">{finding.stage.charAt(0) + finding.stage.slice(1).toLowerCase()}</span>
        {finding.antenaHandles && finding.status !== "PASS" && <span className="antena">Antena does this for you</span>}
      </header>
      {finding.status !== "PASS" && <p className="fix">{finding.fix}</p>}
      {finding.why && <p className="why">{finding.why}</p>}
      <details>
        <summary>What we saw</summary>
        <pre>{finding.evidence}</pre>
      </details>
    </li>
  );
}
