import ErrorView from "@/components/error/error_view";

export default function NotFound() {
  return (
    <ErrorView
      status={404}
      title="Không tìm thấy trang"
      message="Đường dẫn bạn truy cập không tồn tại hoặc nội dung đã bị xoá."
    />
  );
}
