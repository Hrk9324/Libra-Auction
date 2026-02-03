package io.github.guennhatking.libra_auction.models;
import java.time.LocalDateTime;

import io.github.guennhatking.libra_auction.enums.Enums;

public class CauHoi {
    private String id; // string(10)
    private PhienDauGia phienDauGia;
    private NguoiDung nguoiHoi;
    private NguoiDung nguoiTraLoi;
    private String noiDungHoi;
    private String noiDungTraLoi;
    private LocalDateTime thoiGianHoi;
    private LocalDateTime thoiGianTraLoi;
    private Enums.TinhTrangCauHoi tinhTrangCauHoi; // "chưa trả lời" | "đã trả lời" | "đã từ chối trả lời"

    public void traLoi(NguoiDung nguoiTraLoi, String noiDungTraLoi) {}
    public void tuChoiTraLoi() {}
    public String getId() { return id; }
    public PhienDauGia getPhienDauGia() { return phienDauGia; }

    public NguoiDung getNguoiHoi() { return nguoiHoi; }
    public NguoiDung getNguoiTraLoi() { return nguoiTraLoi; }
    public String getNoiDungHoi() { return noiDungHoi; }
    public String getNoiDungTraLoi() { return noiDungTraLoi; }
    public LocalDateTime getThoiGianHoi() { return thoiGianHoi; }
    public LocalDateTime getThoiGianTraLoi() { return thoiGianTraLoi; }
    public Enums.TinhTrangCauHoi getTinhTrangCauHoi() { return tinhTrangCauHoi; }

    //id
    public void setId(String id) { this.id = id; }
    //phienDauGia
    public void setPhienDauGia(PhienDauGia phienDauGia) { this.phienDauGia = phienDauGia; }
    //nguoiHoi  
    public void setNguoiHoi(NguoiDung nguoiHoi) { this.nguoiHoi = nguoiHoi; }
    //noiDungHoi
    public void setNoiDungHoi(String noiDungHoi) { this.noiDungHoi = noiDungHoi; }
    //noiDungTraLoi
    public void setNoiDungTraLoi(String noiDungTraLoi) { this.noiDungTraLoi = noiDungTraLoi; }
    //thoiGianHoi
    public void setThoiGianHoi(LocalDateTime thoiGianHoi) { this.thoiGianHoi = thoiGianHoi; }
    //thoiGianTraLoi
    public void setThoiGianTraLoi(LocalDateTime thoiGianTraLoi) { this.thoiGianTraLoi = thoiGianTraLoi; }
    //tinhTrang
    public void setTinhTrangCauHoi(Enums.TinhTrangCauHoi tinhTrangCauHoi) { this.tinhTrangCauHoi = tinhTrangCauHoi; }
}
