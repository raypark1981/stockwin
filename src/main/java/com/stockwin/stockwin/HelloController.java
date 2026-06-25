package com.stockwin.stockwin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * hello-view.fxml과 연결된 컨트롤러 클래스.
 *
 * 역할 분리 원칙:
 *   - FXML  : 화면 구조(레이아웃, 컴포넌트 배치)
 *   - 컨트롤러 : UI 이벤트 처리, 데이터 바인딩, 비즈니스 로직 호출
 */
public class HelloController {

    // =========================================================================
    // @FXML 주입 필드 (fxml의 fx:id 값과 변수명이 일치해야 자동 주입된다)
    // =========================================================================

    /** 전체 탭을 관리하는 TabPane */
    @FXML
    private TabPane mainTabPane;

    // --- 탭 1 (전일 상친 목록) 관련 컴포넌트 ---

    /** 종목 목록 테이블 */
    @FXML
    private TableView<StockItem> stockTable;

    /** 상단 새로고침 버튼 */
    @FXML
    private Button refreshButton;

    /** 상단 필터 콤보박스 */
    @FXML
    private ComboBox<String> filterComboBox;

    // =========================================================================
    // 일반 필드
    // =========================================================================

    /** 탭 드래그 분리/합치기 유틸리티. setStage() 호출 시 초기화된다. */
    private TabDetachUtil tabDetachUtil;

    // =========================================================================
    // 데이터
    // =========================================================================

    /**
     * ObservableList: JavaFX 전용 리스트.
     * 항목을 추가/삭제/변경하면 TableView가 자동으로 화면을 갱신한다.
     * 일반 ArrayList는 이 자동 갱신 기능이 없다.
     */
    private final ObservableList<StockItem> stockList = FXCollections.observableArrayList();

    // =========================================================================
    // 초기화
    // =========================================================================

    /**
     * JavaFX가 fxml 로딩 완료 후 자동 호출.
     * @FXML 주입이 끝난 시점이므로 모든 컴포넌트를 안전하게 사용할 수 있다.
     */
    @FXML
    public void initialize() {
        setupFilterComboBox();  // 필터 항목 설정
        setupStockTable();      // 테이블 컬럼/색상/클릭 설정
        loadSampleData();       // 테스트용 샘플 데이터 로드
    }

    /**
     * HelloApplication에서 fxmlLoader.load() 이후에 호출한다.
     *
     * Stage가 initialize() 시점에는 아직 존재하지 않아서
     * 별도 메서드로 나중에 전달받는 방식을 사용한다.
     * TabDetachUtil은 Stage 참조가 필요하므로 여기서 초기화한다.
     *
     * @param stage 메인 창 Stage
     */
    public void setStage(Stage stage) {
        tabDetachUtil = new TabDetachUtil(mainTabPane, stage);
    }

    // =========================================================================
    // 탭 1: 전일 상친 목록 설정
    // =========================================================================

    /** 필터 ComboBox 항목 및 이벤트 초기화 */
    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "전체", "코스피", "코스닥", "투자주의 제외"
        ));
        filterComboBox.setValue("전체");

        // 선택 변경 시 호출 (추후 실제 필터링 로직 연결)
        filterComboBox.setOnAction(e -> {
            String selected = filterComboBox.getValue();
            System.out.println("필터 변경: " + selected);
            // TODO: stockList를 필터 조건에 맞게 갱신
        });
    }

    /** TableView 전체 설정 */
    private void setupStockTable() {
        setupColumns();    // 컬럼 정의 및 추가
        setupRowColors();  // 행 배경색 조건
        setupRowClick();   // 클릭 이벤트

        // 준비한 데이터 리스트를 테이블에 연결
        // 이후 stockList에 항목을 추가하면 테이블에 자동 반영된다
        stockTable.setItems(stockList);
    }

    /**
     * 테이블 컬럼 정의.
     *
     * PropertyValueFactory("필드명") 사용 규칙:
     *   "stockCode" → StockItem.stockCodeProperty() 메서드를 자동으로 찾는다.
     *   즉 StockItem의 xxxProperty() 메서드명과 문자열이 일치해야 한다.
     */
    private void setupColumns() {

        // 숫자 포맷터: 1234567 → "1,234,567" (한국 로케일 기준 콤마 구분)
        NumberFormat numFmt = NumberFormat.getNumberInstance(Locale.KOREA);

        // --- 종목코드 ---
        TableColumn<StockItem, String> codeCol = new TableColumn<>("종목코드");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("stockCode"));
        codeCol.setPrefWidth(90);
        codeCol.setStyle("-fx-alignment: CENTER;");

        // --- 종목명 ---
        TableColumn<StockItem, String> nameCol = new TableColumn<>("종목명");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("stockName"));
        nameCol.setPrefWidth(120);

        // --- 전일 종가 (숫자 포맷 적용) ---
        TableColumn<StockItem, Long> prevCloseCol = new TableColumn<>("전일 종가");
        prevCloseCol.setCellValueFactory(new PropertyValueFactory<>("prevClosePrice"));
        prevCloseCol.setPrefWidth(100);
        // CellFactory: 기본 toString 대신 원하는 형태로 표시하려면 직접 구현 - 가져온 값을 화면에 어떻게 보여줄지 정하는 거야.
        prevCloseCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                // col은 입력값으로 받지만 사용하지 않음.
                // new TableCell<>()로 셀 객체를 만들고,
                // 그 안에서 updateItem()을 오버라이드한 상태로 반환한다.
                super.updateItem(value, empty);  // 반드시 super 먼저 호출
                if (empty || value == null) {
                    setText(null);
                } else {
                    // "73,400" 형식으로 표시
                    setText(numFmt.format(value));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- 전일 등락률 (양수=빨강, 음수=파랑 - 한국 주식 관례) ---
        TableColumn<StockItem, Double> changeRateCol = new TableColumn<>("전일 등락률");
        changeRateCol.setCellValueFactory(new PropertyValueFactory<>("prevChangeRate"));
        changeRateCol.setPrefWidth(100);
        changeRateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // %+.2f: 부호(+/-) 포함, 소수점 2자리
                    setText(String.format("%+.2f%%", value));

                    // 한국 증권 색상 관례: 상승=빨강, 하락=파랑
                    if (value > 0) {
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #CC0000; -fx-font-weight: bold;");
                    } else if (value < 0) {
                        setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #0000CC; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-alignment: CENTER-RIGHT;");
                    }
                }
            }
        });

        // --- 전일 거래량 ---
        TableColumn<StockItem, Long> volumeCol = new TableColumn<>("전일 거래량");
        volumeCol.setCellValueFactory(new PropertyValueFactory<>("prevVolume"));
        volumeCol.setPrefWidth(110);
        volumeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(numFmt.format(value));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- 시가총액 (억원 단위) ---
        TableColumn<StockItem, Long> marketCapCol = new TableColumn<>("시가총액(억)");
        marketCapCol.setCellValueFactory(new PropertyValueFactory<>("marketCap"));
        marketCapCol.setPrefWidth(110);
        marketCapCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(numFmt.format(value) + "억");
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- PER ---
        TableColumn<StockItem, Double> perCol = new TableColumn<>("PER");
        perCol.setCellValueFactory(new PropertyValueFactory<>("per"));
        perCol.setPrefWidth(70);
        perCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", value));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- 유통주식수 ---
        TableColumn<StockItem, Long> floatingCol = new TableColumn<>("유통주식수");
        floatingCol.setCellValueFactory(new PropertyValueFactory<>("floatingShares"));
        floatingCol.setPrefWidth(110);
        floatingCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(numFmt.format(value));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- 거래량 증가율 ---
        TableColumn<StockItem, Double> volumeRateCol = new TableColumn<>("거래량 증가율");
        volumeRateCol.setCellValueFactory(new PropertyValueFactory<>("volumeIncreaseRate"));
        volumeRateCol.setPrefWidth(110);
        volumeRateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%+.1f%%", value));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            }
        });

        // --- 테마/섹터 ---
        TableColumn<StockItem, String> sectorCol = new TableColumn<>("테마/섹터");
        sectorCol.setCellValueFactory(new PropertyValueFactory<>("sector"));
        sectorCol.setPrefWidth(150);

        // --- 투자주의/경고 (경고 종목은 주황색 강조) ---
        TableColumn<StockItem, String> warningCol = new TableColumn<>("투자주의/경고");
        warningCol.setCellValueFactory(new PropertyValueFactory<>("investmentWarning"));
        warningCol.setPrefWidth(110);
        warningCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null || "-".equals(value)) {
                    setText("-");
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(value);
                    // 투자주의/경고는 주황색 볼드로 강조
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #FF6600; -fx-font-weight: bold;");
                }
            }
        });

        // --- 주문 예상 목록 담기 버튼 ---
        // Void 타입: 이 컬럼은 데이터 값이 없고 버튼만 표시하므로 타입을 Void로 지정
        TableColumn<StockItem, Void> addBtnCol = new TableColumn<>("담기");
        addBtnCol.setPrefWidth(70);
        addBtnCol.setStyle("-fx-alignment: CENTER;");
        addBtnCol.setCellFactory(col -> new TableCell<>() {

            // 각 셀(행)마다 버튼 인스턴스를 하나씩 생성
            private final Button btn = new Button("담기");

            // 인스턴스 초기화 블록: 생성자 대신 사용, btn 생성 직후 한 번 실행
            {
                btn.setStyle("-fx-font-size: 11px;");
                btn.setOnAction(e -> {
                    // getIndex(): 현재 셀의 행 번호
                    StockItem item = getTableView().getItems().get(getIndex());
                    onAddToOrderList(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // 빈 행(데이터 없는 여백 행)에는 버튼을 표시하지 않음
                setGraphic(empty ? null : btn);
            }
        });

        // 정의한 컬럼들을 순서대로 테이블에 추가
        stockTable.getColumns().addAll(
                codeCol, nameCol, prevCloseCol, changeRateCol,
                volumeCol, marketCapCol, perCol, floatingCol,
                volumeRateCol, sectorCol, warningCol, addBtnCol
        );
    }

    /**
     * 행(Row) 배경색 조건 설정.
     *
     * RowFactory: TableView가 새 행을 만들 때 호출하는 팩토리.
     * updateItem()은 행의 데이터가 바뀔 때마다 다시 호출된다.
     *
     * 우선순위: 투자주의/경고 > 상한가 > 일반 상승 > 하락 > 보합
     */
    private void setupRowColors() {
        stockTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(StockItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");  // 빈 행은 기본 스타일로 초기화
                    return;
                }

                if (!"-".equals(item.getInvestmentWarning())) {
                    // 투자주의/경고 종목: 노란색 배경
                    setStyle("-fx-background-color: #FFF9C4;");
                } else if (item.getPrevChangeRate() >= 29.0) {
                    // 상한가(코스피/코스닥 기준 약 +29.9%): 진한 분홍색
                    setStyle("-fx-background-color: #FFCDD2;");
                } else if (item.getPrevChangeRate() > 0) {
                    // 일반 상승: 연한 분홍색
                    setStyle("-fx-background-color: #FFF0F0;");
                } else if (item.getPrevChangeRate() < 0) {
                    // 하락: 연한 파란색
                    setStyle("-fx-background-color: #F0F0FF;");
                } else {
                    // 보합 or 해당 없음
                    setStyle("");
                }
            }
        });
    }

    /**
     * 행 클릭 시 종목 상세 탭 열기.
     *
     * getClickCount() == 1 : 단일 클릭
     * getClickCount() == 2 : 더블 클릭
     */
    private void setupRowClick() {
        stockTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                StockItem selected = stockTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openStockDetailTab(selected.getStockCode(), selected.getStockName());
                }
            }
        });
    }

    // =========================================================================
    // 이벤트 핸들러
    // =========================================================================

    /**
     * 새로고침 버튼 클릭.
     * fxml에서 onAction="#onRefreshClick"으로 연결됨.
     */
    @FXML
    private void onRefreshClick() {
        // TODO: 실제 API 호출로 교체 예정
        System.out.println("전일 상친 목록 새로고침");
        loadSampleData();
    }

    /** 담기 버튼 클릭 - 주문 예상 목록(탭 3)에 추가 */
    private void onAddToOrderList(StockItem item) {
        // TODO: 탭 3의 주문 예상 목록에 실제로 추가하는 로직으로 교체
        System.out.println("주문 예상 목록 담기: " + item.getStockCode() + " " + item.getStockName());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("담기 완료");
        alert.setHeaderText(null);
        alert.setContentText(item.getStockName() + "을(를) 주문 예상 목록에 담았습니다.");
        alert.show();
    }

    // =========================================================================
    // 데이터 로드
    // =========================================================================

    /**
     * 테스트용 샘플 데이터.
     * 실제 서비스에서는 증권사 API를 호출해서 stockList를 채운다.
     */
    private void loadSampleData() {
        stockList.clear();
        stockList.addAll(
            // 종목코드, 종목명, 전일종가, 등락률, 거래량, 시총(억), PER, 유통주식수, 거래량증가율, 섹터, 투자주의
            new StockItem("005930", "삼성전자",  73400,  29.89, 45_234_567L, 438_234L, 15.2, 5_969_782_550L, 320.5, "반도체/전자",    "-"),
            new StockItem("000660", "SK하이닉스", 187500, 29.95, 12_345_678L, 136_521L, 22.4,   728_002_365L, 280.3, "반도체",         "-"),
            new StockItem("035420", "NAVER",     232500,  5.23,  3_456_789L,  38_234L, 28.6,   163_788_032L, 125.8, "인터넷/플랫폼",  "투자주의"),
            new StockItem("051910", "LG화학",    345000, -1.43,  1_234_567L,  24_321L, 18.9,    70_592_343L,  45.2, "화학/배터리",    "-"),
            new StockItem("006400", "삼성SDI",   420000, -3.21,    987_654L,  28_765L, 35.1,    66_828_171L,  38.7, "배터리",         "-")
        );
    }

    // =========================================================================
    // 공통: 종목 상세 탭 열기
    // =========================================================================

    /**
     * 종목 상세 탭을 동적으로 생성하거나 기존 탭으로 이동.
     *
     * 규칙: 같은 종목코드(tab.getId())가 이미 열려 있으면 새 탭을 만들지 않고
     * 기존 탭을 선택 상태로 전환한다.
     *
     * @param stockCode 종목코드 (탭 식별자로 사용)
     * @param stockName 종목명 (탭 제목에 표시)
     */
    public void openStockDetailTab(String stockCode, String stockName) {

        // 이미 열려 있는 탭인지 확인
        for (Tab tab : mainTabPane.getTabs()) {
            if (stockCode.equals(tab.getId())) {
                mainTabPane.getSelectionModel().select(tab);
                return;
            }
        }

        // 새 탭 생성
        Tab detailTab = new Tab(stockName + " [" + stockCode + "]");
        detailTab.setId(stockCode);   // 종목코드를 탭 식별자로 사용 (중복 방지 기준)
        detailTab.setClosable(true);  // 사용자가 직접 닫을 수 있음

        // 임시 내용 (나중에 상세 화면 fxml로 교체 예정)
        StackPane content = new StackPane();
        content.getChildren().add(new Label(stockName + " (" + stockCode + ") 상세 화면"));
        detailTab.setContent(content);

        mainTabPane.getTabs().add(detailTab);
        mainTabPane.getSelectionModel().selectLast();  // 새 탭으로 포커스 이동

        // 주문 상세 탭에만 드래그 분리 기능 추가
        // (탭 1~4는 closable=false이므로 makeDetachable을 호출하지 않음)
        if (tabDetachUtil != null) {
            tabDetachUtil.makeDetachable(detailTab);
        }
    }
}