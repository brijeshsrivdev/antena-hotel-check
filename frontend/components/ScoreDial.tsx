/** 0–100 score as a conic ring; null (nothing could be observed) renders a dash. */
export function ScoreDial({ score }: { score: number | null }) {
  const value = score ?? 0;
  const colour = score == null ? "var(--unknown)" : score >= 80 ? "var(--pass)" : score >= 50 ? "var(--partial)" : "var(--fail)";
  return (
    <div className="dial" role="img" aria-label={score == null ? "No score: nothing could be observed" : `Score ${score} out of 100`}
         style={{ background: `conic-gradient(${colour} ${value * 3.6}deg, var(--line) 0deg)` }}>
      <div className="value">
        <strong>{score == null ? "–" : score}</strong>
        <span>out of 100</span>
      </div>
    </div>
  );
}
