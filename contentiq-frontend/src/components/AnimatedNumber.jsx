import { useEffect, useRef, useState } from 'react';

const easeOutCubic = (t) => 1 - Math.pow(1 - t, 3);

export default function AnimatedNumber({
  value = 0,
  duration = 900,
  format = (n) => Math.round(n).toLocaleString(),
  className = '',
}) {
  const [display, setDisplay] = useState(0);
  const startRef = useRef(0);
  const fromRef = useRef(0);
  const rafRef = useRef(null);

  useEffect(() => {
    const target = Number(value) || 0;
    fromRef.current = display;
    startRef.current = performance.now();

    const tick = (now) => {
      const elapsed = now - startRef.current;
      const t = Math.min(1, elapsed / duration);
      const eased = easeOutCubic(t);
      const next = fromRef.current + (target - fromRef.current) * eased;
      setDisplay(next);
      if (t < 1) {
        rafRef.current = requestAnimationFrame(tick);
      } else {
        setDisplay(target);
      }
    };

    if (rafRef.current) cancelAnimationFrame(rafRef.current);
    rafRef.current = requestAnimationFrame(tick);

    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [value, duration]);

  return <span className={`tabular-nums ${className}`}>{format(display)}</span>;
}
