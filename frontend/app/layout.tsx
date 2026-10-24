import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Antena Hotel Check",
  description: "Hospitality-specific digital experience analyzer for hotels.",
  robots: { index: false, follow: false },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
