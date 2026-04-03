package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class KetHopThuocTinh {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private TaiSan taiSan;

    @ManyToOne
    private ThuocTinhChuanHoa thuocTinhChuanHoa;

    // CONSTRUCTOR
    protected KetHopThuocTinh() {
    }

    public KetHopThuocTinh(TaiSan taiSan, ThuocTinhChuanHoa thuocTinhChuanHoa) {
        this.taiSan = taiSan;
        this.thuocTinhChuanHoa = thuocTinhChuanHoa;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    public ThuocTinhChuanHoa getThuocTinhChuanHoa() {
        return thuocTinhChuanHoa;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }

    public void setThuocTinhChuanHoa(ThuocTinhChuanHoa thuocTinhChuanHoa) {
        this.thuocTinhChuanHoa = thuocTinhChuanHoa;
    }
}
