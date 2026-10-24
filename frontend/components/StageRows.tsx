import type { StageScore } from "@/lib/siteCheck";

/** One row per journey stage: label, bar, score; the counts say how many checks were observed. */
export function StageRows({ stages }: { stages: StageScore[] }) {
  return (
    <ul className="stages" aria-label="Scores by journey stage">
      {stages.map((s) => (
        <li key={s.stage}>
          <div>
            <strong>{s.label}</strong>
            <div className="counts">weight {s.weight} · {s.pass} pass, {s.partial} partly, {s.fail} fix{s.unknown ? `, ${s.unknown} unknown` : ""}</div>
          </div>
          <div className="bar" aria-hidden="true"><div style={{ width: `${s.score ?? 0}%` }} /></div>
          <div>{s.score == null ? "–" : s.score}</div>
        </li>
      ))}
    </ul>
  );
}
