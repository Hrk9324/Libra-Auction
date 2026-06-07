"use client";

import ErrorView from "@/components/error/error_view";
import { getErrorMessage, getErrorStatus, getErrorTitle } from "@/lib/app_error";

export default function Error({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  const status = getErrorStatus(error);

  return (
    <ErrorView
      status={status}
      title={getErrorTitle(status)}
      message={getErrorMessage(error)}
      onRetry={reset}
    />
  );
}
