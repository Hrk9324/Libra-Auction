"use client";

import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";
import "./globals.css";

export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  const status = getErrorStatus(error);

  return (
    <html lang="en">
      <body>
        <ErrorView
          status={status}
          title={getErrorTitle(status)}
          message={getErrorMessage(error)}
          onRetry={reset}
        />
      </body>
    </html>
  );
}
