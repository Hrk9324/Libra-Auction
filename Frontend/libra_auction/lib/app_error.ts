import type { ServerAPIResponse } from "@/types/serser_API_response";

export class AppError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = "AppError";
    this.status = status;
  }
}

type ErrorLikeResponse = Partial<ServerAPIResponse<unknown>> & {
  message?: string;
};

export function createAppErrorFromResponse(
  response: ErrorLikeResponse,
  fallbackMessage = "Không thể xử lý yêu cầu. Vui lòng thử lại sau."
) {
  return new AppError(
    response.status || 500,
    response.errorMessage || response.message || fallbackMessage
  );
}

export function getErrorStatus(error: unknown) {
  if (error instanceof AppError) return error.status;
  if (error && typeof error === "object" && "status" in error) {
    const status = Number((error as { status?: unknown }).status);
    if (Number.isInteger(status) && status >= 100 && status <= 599) return status;
  }
  return 500;
}

export function getErrorMessage(
  error: unknown,
  fallbackMessage = "Không thể xử lý yêu cầu. Vui lòng thử lại sau."
) {
  if (error instanceof Error && error.message) return error.message;
  if (error && typeof error === "object" && "message" in error) {
    const message = (error as { message?: unknown }).message;
    if (typeof message === "string" && message.trim()) return message;
  }
  return fallbackMessage;
}

export function getErrorTitle(status: number) {
  if (status === 401) return "Bạn cần đăng nhập";
  if (status === 403) return "Không có quyền truy cập";
  if (status === 404) return "Không tìm thấy nội dung";
  if (status === 503) return "Không thể kết nối máy chủ";
  return "Đã xảy ra lỗi";
}
