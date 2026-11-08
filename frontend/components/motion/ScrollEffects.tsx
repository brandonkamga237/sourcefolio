"use client";

import { usePathname } from "next/navigation";
import { useEffect } from "react";

// Loaded after the page is up: GSAP never delays the first paint on a low-end phone, and a page
// whose script fails to load simply stays still, with everything visible.
export function ScrollEffects() {
  const pathname = usePathname();

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    let disposed = false;
    let cleanup = () => {};

    void Promise.all([import("gsap"), import("gsap/ScrollTrigger")]).then(([{ gsap }, { ScrollTrigger }]) => {
      if (disposed) return;
      gsap.registerPlugin(ScrollTrigger);

      const once = (attribute: string) =>
        [...document.querySelectorAll<HTMLElement>(`[${attribute}]`)].filter((element) => {
          if (element.dataset.fx?.includes(attribute)) return false;
          element.dataset.fx = `${element.dataset.fx ?? ""} ${attribute}`;
          return true;
        });

      const scan = () => {
        for (const element of once("data-reveal")) {
          gsap.from(element, { autoAlpha: 0, y: 48, duration: 0.9, ease: "power3.out", scrollTrigger: { trigger: element, start: "top 88%", once: true } });
        }
        for (const element of once("data-stagger")) {
          gsap.from(element.children, { autoAlpha: 0, y: 36, duration: 0.7, ease: "power3.out", stagger: 0.09, scrollTrigger: { trigger: element, start: "top 85%", once: true } });
        }
        for (const element of once("data-count")) {
          const target = Number(element.dataset.count);
          const format = new Intl.NumberFormat(document.documentElement.lang);
          const state = { value: 0 };
          gsap.to(state, {
            value: target,
            duration: 1.6,
            ease: "power2.out",
            onUpdate: () => {
              element.textContent = format.format(Math.round(state.value));
            },
            scrollTrigger: { trigger: element, start: "top 92%", once: true },
          });
        }
        for (const element of once("data-wipe")) {
          gsap.fromTo(element, { clipPath: "inset(0 100% 0 0)" }, { clipPath: "inset(0 0% 0 0)", duration: 1.6, ease: "power2.inOut", scrollTrigger: { trigger: element, start: "top 88%", once: true } });
        }
        for (const path of once("data-draw") as unknown as SVGPathElement[]) {
          const length = path.getTotalLength();
          gsap.fromTo(path, { strokeDasharray: length, strokeDashoffset: length }, { strokeDashoffset: 0, ease: "none", scrollTrigger: { trigger: path.ownerSVGElement, start: "top 70%", end: "bottom 60%", scrub: 0.5 } });
        }
        for (const element of once("data-light")) {
          gsap.fromTo(element, { scale: 0.6, opacity: 0.35 }, { scale: 1, opacity: 1, duration: 0.5, ease: "back.out(2.5)", scrollTrigger: { trigger: element, start: "top 62%", toggleActions: "play none none reverse" } });
        }
        for (const element of once("data-float")) {
          gsap.to(element, { yPercent: -14, ease: "none", scrollTrigger: { trigger: element, start: "top bottom", end: "bottom top", scrub: 0.8 } });
        }
        ScrollTrigger.refresh();
      };

      scan();
      // Sections behind a Suspense boundary arrive after the first scan.
      let pending = 0;
      const watcher = new MutationObserver(() => {
        window.clearTimeout(pending);
        pending = window.setTimeout(scan, 150);
      });
      const main = document.getElementById("content");
      if (main) watcher.observe(main, { childList: true, subtree: true });

      const glow = (event: PointerEvent) => {
        const card = (event.target as HTMLElement).closest?.<HTMLElement>(".glow-card");
        if (!card) return;
        const box = card.getBoundingClientRect();
        card.style.setProperty("--x", `${event.clientX - box.left}px`);
        card.style.setProperty("--y", `${event.clientY - box.top}px`);
      };
      document.addEventListener("pointermove", glow, { passive: true });

      cleanup = () => {
        window.clearTimeout(pending);
        watcher.disconnect();
        document.removeEventListener("pointermove", glow);
        ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        gsap.killTweensOf("*");
        gsap.set("[data-reveal], [data-stagger] > *, [data-wipe]", { clearProps: "all" });
        document.querySelectorAll<HTMLElement>("[data-fx]").forEach((element) => delete element.dataset.fx);
      };
    });

    return () => {
      disposed = true;
      cleanup();
    };
  }, [pathname]);

  return null;
}
