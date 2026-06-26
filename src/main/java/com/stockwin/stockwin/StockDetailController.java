package com.stockwin.stockwin;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * stock-detail-view.fxml 컨트롤러.
 *
 * 종목 하나에 대한 상세 정보를 표시한다.
 * openStockDetailTab()에서 fxml 로드 후 initData()를 호출해서 종목 정보를 전달한다.
 */
public class StockDetailController {

    // =========================================================================
    // @FXML 주입: 상단 기본 정보
    // =========================================================================
    @FXML private Label lblStockCode;
    @FXML private Label lblStockName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblChangeRate;
    @FXML private Label lblStatus;

    @FXML private Label lblPrevClose;
    @FXML private Label lblOpen;
    @FXML private Label lblHigh;
    @FXML private Label lblLow;
    @FXML private Label lblVolume;
    @FXML private Label lblTurnover;

    // =========================================================================
    // @FXML 주입: 매수 영역
    // =========================================================================
    @FXML private Label lblBuyStartPrice;
    @FXML private Label lblBuyCurrentPrice;
    @FXML private Label lblBuyOrderStatus;
    @FXML private Label lblBuyFilled;
    @FXML private Label lblAvgBuyPrice;

    // 가격 기준 버튼 (토글 그룹으로 동작)
    @FXML private Button btnBaseCurrentPrice;
    @FXML private Button btnBaseStartPrice;
    @FXML private Button btnBuyDirect;
    @FXML private TextField tfBuyPrice;

    // 예상 매수 요약 박스
    @FXML private Label lblBuyMode;
    @FXML private Label lblCalcPrice;
    @FXML private Label lblCalcQty;
    @FXML private Label lblCalcAmount;

    // 예수금 / 잔액 / 주문가능수량
    @FXML private Label lblDeposit;
    @FXML private Label lblRemaining;
    @FXML private Label lblMaxQty;

    // 수량 입력 + 증감 버튼
    @FXML private TextField tfBuyQty;
    @FXML private Button btnQtyUp;
    @FXML private Button btnQtyDown;

    private int selectedBuyRatePct = 5; // 수익률 % 버튼 선택값 (기본 5%)

    // 수익률 % 버튼 (토글 그룹으로 동작)
    @FXML private Button btnBuy3Pct;
    @FXML private Button btnBuy5Pct;
    @FXML private Button btnBuy7Pct;
    @FXML private Button btnBuy10Pct;

    // 손실방어율 % 버튼 (토글 그룹으로 동작)
    @FXML private Button btnDefense3Pct;
    @FXML private Button btnDefense5Pct;
    @FXML private Button btnDefense7Pct;
    @FXML private Button btnDefense10Pct;

    // =========================================================================
    // @FXML 주입: 매도 영역
    // =========================================================================
    @FXML private Label lblHoldQty;
    @FXML private Label lblSellAvgBuyPrice;
    @FXML private Label lblTargetRate;
    @FXML private Label lblTargetPrice;
    @FXML private Label lblStopLossPrice;
    @FXML private Label lblCurrentRate;
    @FXML private Label lblSellOrderStatus;
    @FXML private Label lblSellFilled;
    @FXML private Label lblUnfilledQty;

    // =========================================================================
    // @FXML 주입: 호가 영역
    // =========================================================================
    @FXML private TableView<HokaRow> askTable;   // 매도 호가 테이블
    @FXML private TableView<HokaRow> bidTable;   // 매수 호가 테이블
    @FXML private Label lblHokaCurrentPrice;
    @FXML private Label lblStrength;
    @FXML private ListView<String> tradeList;    // 실시간 체결 내역

    // =========================================================================
    // 데이터 모델
    // =========================================================================

    /**
     * 호가 한 행(row)을 표현하는 record (Java 16+).
     * record는 불변 데이터 클래스를 간결하게 선언하는 문법이다.
     * 자동으로 생성자, getter, equals, hashCode, toString을 만들어준다.
     *
     * @param price  가격 문자열 (포맷 완료된 상태)
     * @param volume 잔량 문자열
     */
    private record HokaRow(String price, String volume) {}

    private final NumberFormat numFmt = NumberFormat.getNumberInstance(Locale.KOREA);

    // =========================================================================
    // 초기화
    // =========================================================================

    // 가격 기준 버튼 스타일 (청회색 계열)
    private static final String BASE_BTN_OFF =
            "-fx-background-color: #ECEFF1; -fx-text-fill: #546E7A; -fx-font-size: 11px; -fx-padding: 2 8 2 8;";
    private static final String BASE_BTN_ON =
            "-fx-background-color: #37474F; -fx-text-fill: #FFFFFF; -fx-font-size: 11px; -fx-padding: 2 8 2 8;" +
            "-fx-border-color: #263238; -fx-border-width: 2; -fx-border-radius: 3;";

    // 수익률 버튼 스타일 (파랑 계열)
    private static final String RATE_BTN_OFF =
            "-fx-background-color: #E3F2FD; -fx-text-fill: #1565C0; -fx-font-weight: bold;";
    private static final String RATE_BTN_ON =
            "-fx-background-color: #1565C0; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;" +
            "-fx-border-color: #0D47A1; -fx-border-width: 2; -fx-border-radius: 3;";

    // 손실방어율 버튼 스타일 (주황 계열)
    private static final String DEFENSE_BTN_OFF =
            "-fx-background-color: #FFF3E0; -fx-text-fill: #E65100; -fx-font-weight: bold;";
    private static final String DEFENSE_BTN_ON =
            "-fx-background-color: #E65100; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;" +
            "-fx-border-color: #BF360C; -fx-border-width: 2; -fx-border-radius: 3;";

    @FXML
    public void initialize() {
        setupHokaTables();        // 호가 테이블 컬럼 정의
        setupBasePriceButtons();  // 가격 기준 버튼 토글 설정
        setupBuyRateButtons();    // 수익률 % 버튼 토글 설정
        setupDefenseButtons();    // 손실방어율 % 버튼 토글 설정
        setupQtyButtons();        // 수량 ▲▼ 버튼
    }

    /**
     * 수익률 % 버튼을 토글 그룹으로 설정한다.
     * 버튼을 클릭하면 해당 버튼만 활성 스타일로 바뀌고, 나머지는 비활성 스타일로 초기화된다.
     *
     * List.of()는 불변 리스트를 만드는 Java 9+ 팩토리 메서드.
     * 람다 안에서 allBtns를 캡처할 때 effectively final이어야 하는 제약을 만족한다.
     */
    private void setupBasePriceButtons() {
        List<Button> allBtns = List.of(btnBaseCurrentPrice, btnBaseStartPrice, btnBuyDirect);

        allBtns.forEach(btn -> btn.setStyle(BASE_BTN_OFF));
        btnBaseCurrentPrice.setStyle(BASE_BTN_ON); // 기본값: 현재가
        tfBuyPrice.setDisable(true);               // 기본값: 비활성

        // 가격/수량 변경 시 요약 재계산
        tfBuyPrice.textProperty().addListener((obs, oldVal, newVal) -> recalcBuyPrice());
        tfBuyQty.textProperty().addListener((obs, oldVal, newVal) -> recalcSummary());

        applyBasePrice(btnBaseCurrentPrice);        // 기본값: 현재가 가격 채움

        allBtns.forEach(btn -> btn.setOnAction(e -> {
            allBtns.forEach(b -> b.setStyle(BASE_BTN_OFF));
            btn.setStyle(BASE_BTN_ON);
            tfBuyPrice.setDisable(btn != btnBuyDirect);
            if (btn != btnBuyDirect) {
                applyBasePrice(btn);
            } else {
                tfBuyPrice.clear();
                lblBuyMode.setText("직접 입력");
                lblCalcPrice.setText("-");
            }
        }));
    }

    private void applyBasePrice(Button activeBtn) {
        String mode;
        if (activeBtn == btnBaseCurrentPrice) {
            tfBuyPrice.setText(lblBuyCurrentPrice.getText());
            mode = "현재가 대비(수익률)";
        } else {
            tfBuyPrice.setText(lblBuyStartPrice.getText());
            mode = "시작 예상가 대비(수익률)";
        }
        lblBuyMode.setText(mode);
        recalcBuyPrice();
    }

    /** 기준가(tfBuyPrice) × (1 - 수익률%) → lblCalcPrice 갱신 후 요약 재계산 */
    private void recalcBuyPrice() {
        String raw = tfBuyPrice.getText().replaceAll("[^0-9]", "");
        if (raw.isEmpty()) {
            lblCalcPrice.setText("-");
            recalcSummary();
            return;
        }
        try {
            long base = Long.parseLong(raw);
            long calc = Math.round(base * (1.0 - selectedBuyRatePct / 100.0));
            lblCalcPrice.setText(numFmt.format(calc));
        } catch (NumberFormatException ex) {
            lblCalcPrice.setText("-");
        }
        recalcSummary();
    }

    /** lblCalcPrice × tfBuyQty → lblCalcAmount, lblCalcQty, lblRemaining 갱신 */
    private void recalcSummary() {
        String priceRaw   = lblCalcPrice.getText().replaceAll("[^0-9]", "");
        String qtyRaw     = tfBuyQty.getText().replaceAll("[^0-9]", "");
        String depositRaw = lblDeposit.getText().replaceAll("[^0-9]", "");
        long deposit = depositRaw.isEmpty() ? 0 : Long.parseLong(depositRaw);

        // 주문가능수량: 예수금 ÷ 계산가격 (수량과 무관하게 항상 갱신)
        if (!priceRaw.isEmpty()) {
            try {
                long price = Long.parseLong(priceRaw);
                lblMaxQty.setText(price > 0 ? numFmt.format(deposit / price) + " 주" : "-");
            } catch (NumberFormatException ex) {
                lblMaxQty.setText("-");
            }
        } else {
            lblMaxQty.setText("-");
        }

        if (priceRaw.isEmpty() || qtyRaw.isEmpty()) {
            lblCalcQty.setText(qtyRaw.isEmpty() ? "-" : qtyRaw);
            lblCalcAmount.setText("-");
            lblRemaining.setText(numFmt.format(deposit) + " 원");
            return;
        }
        try {
            long price     = Long.parseLong(priceRaw);
            long qty       = Long.parseLong(qtyRaw);
            long amount    = price * qty;
            long remaining = deposit - amount;

            lblCalcQty.setText(numFmt.format(qty));
            lblCalcAmount.setText(numFmt.format(amount) + " 원");
            lblRemaining.setText(numFmt.format(remaining) + " 원");
        } catch (NumberFormatException ex) {
            lblCalcQty.setText("-");
            lblCalcAmount.setText("-");
        }
    }

    private void setupBuyRateButtons() {
        List<Button> allBtns = List.of(btnBuy3Pct, btnBuy5Pct, btnBuy7Pct, btnBuy10Pct);
        List<Integer>    rates = List.of(3, 5, 7, 10);

        allBtns.forEach(btn -> btn.setStyle(RATE_BTN_OFF));
        btnBuy5Pct.setStyle(RATE_BTN_ON);

        for (int i = 0; i < allBtns.size(); i++) {
            int rate = rates.get(i);
            Button btn = allBtns.get(i);
            btn.setOnAction(e -> {
                allBtns.forEach(b -> b.setStyle(RATE_BTN_OFF));
                btn.setStyle(RATE_BTN_ON);
                selectedBuyRatePct = rate;
                recalcBuyPrice();
            });
        }
    }

    private void setupDefenseButtons() {
        List<Button> allBtns = List.of(btnDefense3Pct, btnDefense5Pct, btnDefense7Pct, btnDefense10Pct);

        // 초기 상태: 모두 비활성 후 7%만 활성
        allBtns.forEach(btn -> btn.setStyle(DEFENSE_BTN_OFF));
        btnDefense7Pct.setStyle(DEFENSE_BTN_ON);

        // 각 버튼 클릭 시 → 자신만 활성, 나머지 비활성
        allBtns.forEach(btn -> btn.setOnAction(e -> {
            allBtns.forEach(b -> b.setStyle(DEFENSE_BTN_OFF));
            btn.setStyle(DEFENSE_BTN_ON);
        }));
    }

    private void setupQtyButtons() {
        tfBuyQty.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("[0-9]*")) return null;
            if (newText.isEmpty()) return change;

            String priceRaw   = lblCalcPrice.getText().replaceAll("[^0-9]", "");
            String depositRaw = lblDeposit.getText().replaceAll("[^0-9]", "");
            if (!priceRaw.isEmpty() && !depositRaw.isEmpty()) {
                try {
                    long price   = Long.parseLong(priceRaw);
                    long deposit = Long.parseLong(depositRaw);
                    long qty     = Long.parseLong(newText);
                    if (price > 0 && qty * price > deposit) return null; // 예수금 초과 차단
                } catch (NumberFormatException ignored) {}
            }
            return change;
        }));
        btnQtyUp.setOnAction(e -> adjustQty(1));
        btnQtyDown.setOnAction(e -> adjustQty(-1));
    }

    private void adjustQty(int delta) {
        String raw = tfBuyQty.getText().replaceAll("[^0-9]", "");
        long qty = raw.isEmpty() ? 0 : Long.parseLong(raw);
        qty = Math.max(0, qty + delta);

        // ▲ 버튼에도 예수금 초과 제한 적용
        String priceRaw   = lblCalcPrice.getText().replaceAll("[^0-9]", "");
        String depositRaw = lblDeposit.getText().replaceAll("[^0-9]", "");
        if (!priceRaw.isEmpty() && !depositRaw.isEmpty()) {
            try {
                long price   = Long.parseLong(priceRaw);
                long deposit = Long.parseLong(depositRaw);
                if (price > 0 && qty * price > deposit) return;
            } catch (NumberFormatException ignored) {}
        }

        tfBuyQty.setText(String.valueOf(qty));
    }

    /**
     * 탭이 열릴 때 외부(HelloController)에서 호출해서 종목 정보를 전달한다.
     *
     * @param stockCode 종목코드
     * @param stockName 종목명
     */
    public void initData(String stockCode, String stockName) {
        // 기본 정보 설정
        lblStockCode.setText(stockCode);
        lblStockName.setText(stockName);

        // 나머지 데이터는 실제 API 연동 전까지 샘플로 채운다
        loadSampleData(stockCode);
    }

    // =========================================================================
    // 호가 테이블 설정
    // =========================================================================

    /**
     * askTable (매도 호가), bidTable (매수 호가) 컬럼을 정의한다.
     *
     * 매도 호가 구조: [잔량] [매도호가] → 높은 가격이 위에 표시
     * 매수 호가 구조: [매수호가] [잔량]  → 높은 가격이 위에 표시
     */
    private void setupHokaTables() {

        // ── 매도 호가 테이블 ──────────────────────────────
        TableColumn<HokaRow, String> askVolumeCol = new TableColumn<>("잔량");
        // ReadOnlyStringWrapper: 단순 문자열을 Property로 감싸는 편의 클래스
        // TableView는 Property 타입을 요구하기 때문에 사용
        askVolumeCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().volume()));
        askVolumeCol.setPrefWidth(80);
        askVolumeCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #0000CC;");

        TableColumn<HokaRow, String> askPriceCol = new TableColumn<>("매도호가");
        askPriceCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().price()));
        askPriceCol.setPrefWidth(100);
        // 매도 호가는 파란색 (하락 방향)
        askPriceCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #0000CC; -fx-font-weight: bold;");

        askTable.getColumns().addAll(askVolumeCol, askPriceCol);
        // 테이블 자체 컬럼 리사이즈: 컬럼들이 전체 너비를 채우도록
        askTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ── 매수 호가 테이블 ──────────────────────────────
        TableColumn<HokaRow, String> bidPriceCol = new TableColumn<>("매수호가");
        bidPriceCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().price()));
        bidPriceCol.setPrefWidth(100);
        // 매수 호가는 빨간색 (상승 방향)
        bidPriceCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #CC0000; -fx-font-weight: bold;");

        TableColumn<HokaRow, String> bidVolumeCol = new TableColumn<>("잔량");
        bidVolumeCol.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().volume()));
        bidVolumeCol.setPrefWidth(80);
        bidVolumeCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #CC0000;");

        bidTable.getColumns().addAll(bidPriceCol, bidVolumeCol);
        bidTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // =========================================================================
    // 상태값 업데이트 메서드 (실시간 데이터 수신 시 호출 예정)
    // =========================================================================

    /**
     * 종목 상태 레이블 텍스트와 배경색을 함께 업데이트한다.
     *
     * 상태에 따라 직관적인 색상으로 표시:
     *   감시중/주문중  → 주황/노랑 계열
     *   체결(성공)     → 초록 계열
     *   종료           → 회색
     */
    public void updateStatus(String status) {
        lblStatus.setText(status);

        String bgColor = switch (status) {
            case "감시중"      -> "#1565C0"; // 파랑
            case "매수주문완료" -> "#E65100"; // 주황
            case "매수체결"    -> "#2E7D32"; // 진한 초록
            case "매도감시중"  -> "#6A1B9A"; // 보라
            case "매도주문완료" -> "#4A148C"; // 진한 보라
            case "매도체결"    -> "#00695C"; // 청록
            case "전략종료"    -> "#424242"; // 진한 회색
            default            -> "#555555"; // 기본 (후보 등)
        };

        lblStatus.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-padding: 3 12 3 12;" +
                "-fx-background-radius: 4;" +
                "-fx-font-weight: bold;"
        );
    }

    /**
     * 현재가와 등락률을 업데이트한다.
     * 한국 주식 관례: 상승=빨강, 하락=파랑
     *
     * @param price      현재가
     * @param changeRate 등락률 (예: +29.89, -1.23)
     */
    public void updatePrice(long price, double changeRate) {
        String priceStr = numFmt.format(price);
        String rateStr  = String.format("%+.2f%%", changeRate);

        lblCurrentPrice.setText(priceStr);
        lblChangeRate.setText(rateStr);
        lblHokaCurrentPrice.setText(priceStr);

        // 색상: 상승=빨강(#FF4444), 하락=파랑(#4444FF), 보합=회색
        String color;
        if (changeRate > 0)      color = "#FF4444";
        else if (changeRate < 0) color = "#4444FF";
        else                     color = "#AAAAAA";

        lblCurrentPrice.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        lblChangeRate.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 15px; -fx-font-weight: bold;");

        // 현재 수익률 색상도 같은 기준으로 처리
        if (changeRate > 0)
            lblCurrentRate.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #CC0000;");
        else if (changeRate < 0)
            lblCurrentRate.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0000CC;");
    }

    // =========================================================================
    // 샘플 데이터 (API 연동 전 테스트용)
    // =========================================================================

    private void loadSampleData(String stockCode) {

        // ── 상단 기본 정보 ──
        lblCurrentPrice.setText("73,400");
        lblChangeRate.setText("+29.89%");
        lblPrevClose.setText("56,500");
        lblOpen.setText("58,000");
        lblHigh.setText("74,100");
        lblLow.setText("57,800");
        lblVolume.setText("45,234,567");
        lblTurnover.setText("3,312억");
        updateStatus("감시중");

        // ── 매수 영역 ──
        lblBuyStartPrice.setText("57,000");
        lblBuyCurrentPrice.setText("57,200");
        lblBuyOrderStatus.setText("대기");
        lblBuyFilled.setText("미체결");
        lblAvgBuyPrice.setText("-");

        // 가격 설정 후 기본 선택(현재가 + 수익률 기본값)으로 lblCalcPrice 갱신
        applyBasePrice(btnBaseCurrentPrice);

        // ── 매도 영역 ──
        lblHoldQty.setText("0 주");
        lblSellAvgBuyPrice.setText("-");
        lblTargetRate.setText("+5.00%");
        lblTargetPrice.setText("60,060");
        lblStopLossPrice.setText("54,150");
        lblCurrentRate.setText("-");
        lblSellOrderStatus.setText("-");
        lblSellFilled.setText("-");
        lblUnfilledQty.setText("-");

        // ── 호가 영역: 매도 10호가 (높은 가격부터) ──
        ObservableList<HokaRow> askData = FXCollections.observableArrayList(
                new HokaRow("58,600", "2,100"),
                new HokaRow("58,500", "1,350"),
                new HokaRow("58,400", "3,720"),
                new HokaRow("58,300", "890"),
                new HokaRow("58,200", "4,560"),
                new HokaRow("58,100", "2,230"),
                new HokaRow("58,000", "6,780"),
                new HokaRow("57,900", "1,100"),
                new HokaRow("57,800", "3,450"),
                new HokaRow("57,700", "2,870")
        );
        askTable.setItems(askData);

        // ── 호가 영역: 매수 10호가 (높은 가격부터) ──
        ObservableList<HokaRow> bidData = FXCollections.observableArrayList(
                new HokaRow("57,600", "5,230"),
                new HokaRow("57,500", "3,410"),
                new HokaRow("57,400", "7,890"),
                new HokaRow("57,300", "2,100"),
                new HokaRow("57,200", "4,560"),
                new HokaRow("57,100", "1,230"),
                new HokaRow("57,000", "8,900"),
                new HokaRow("56,900", "3,670"),
                new HokaRow("56,800", "2,450"),
                new HokaRow("56,700", "6,120")
        );
        bidTable.setItems(bidData);

        // ── 체결강도 ──
        lblHokaCurrentPrice.setText("57,650");
        lblStrength.setText("123.5%");

        // ── 실시간 체결 내역 (최신이 위에 오도록 추가) ──
        ObservableList<String> trades = FXCollections.observableArrayList(
                "09:32:14  57,650  ▲  500주",
                "09:32:11  57,600  ▲  1,200주",
                "09:32:08  57,550  ▼  300주",
                "09:32:05  57,600  ▲  800주",
                "09:32:01  57,500  ▼  200주"
        );
        tradeList.setItems(trades);
    }
}