package com.stockwin.stockwin;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * 탭을 드래그해서 별도 창으로 분리하고 다시 합치는 기능을 담당하는 유틸리티 클래스.
 *
 * 동작 방식:
 *   1. makeDetachable(tab) 호출 → 탭 헤더에 드래그 이벤트 부착
 *   2. 탭 헤더를 50px 이상 드래그 후 마우스를 떼면 새 창(Stage)으로 분리
 *   3. 분리 창의 [탭으로 합치기] 버튼 또는 창 닫기(X) → 원래 TabPane으로 복귀
 *
 * 탭 헤더에 직접 마우스 이벤트를 붙이지 못하는 이유:
 *   TabPane의 헤더 영역은 내부 구현(CSS lookup)으로만 접근 가능해서 불안정하다.
 *   대신 Tab.setGraphic()에 Label을 넣고, 그 Label에 이벤트를 부착하는 방식 사용.
 */
public class TabDetachUtil {

    /** 탭이 소속된 원본 TabPane */
    private final TabPane tabPane;

    /** 메인 창 Stage (분리 창 소유자로 지정에 사용) */
    private final Stage primaryStage;

    public TabDetachUtil(TabPane tabPane, Stage primaryStage) {
        this.tabPane = tabPane;
        this.primaryStage = primaryStage;
    }

    /**
     * 지정한 탭에 드래그-분리 기능을 추가한다.
     *
     * Tab.setText()를 비우고 Tab.setGraphic(Label)로 교체하는 이유:
     * TabPane 헤더의 내부 노드에는 직접 이벤트를 붙이기 어렵다.
     * Graphic에 Label을 올리면 그 Label에 마우스 이벤트를 자유롭게 붙일 수 있다.
     *
     * @param tab 드래그 분리를 활성화할 탭
     */
    public void makeDetachable(Tab tab) {
        String title = tab.getText();

        // 기존 텍스트를 제거하고 Label을 graphic으로 올림
        Label headerLabel = new Label(title);
        tab.setText("");
        tab.setGraphic(headerLabel);

        // 람다 안에서 변경 가능한 변수는 직접 사용 불가 → 배열로 우회
        // (Java 람다 캡처 규칙: effectively final만 허용)
        double[] dragStartPos = new double[2];

        // 마우스 누름: 시작 위치 기록만 한다
        // e.consume() 하지 않음 → TabPane이 탭 선택 이벤트를 정상 처리해야 하므로
        headerLabel.setOnMousePressed(e -> {
            dragStartPos[0] = e.getScreenX();
            dragStartPos[1] = e.getScreenY();
        });

        // 마우스 뗌: 이동 거리를 계산해서 50px 이상이면 드래그 분리
        headerLabel.setOnMouseReleased(e -> {
            double dx = Math.abs(e.getScreenX() - dragStartPos[0]);
            double dy = Math.abs(e.getScreenY() - dragStartPos[1]);

            if (dx > 50 || dy > 50) {
                detachTab(tab, title, e.getScreenX(), e.getScreenY());
                e.consume(); // 분리가 발생한 경우에만 이벤트 소비
            }
        });

        // 더블클릭: 탭 분리
        // getClickCount() == 2 : 빠르게 두 번 클릭한 경우
        //
        // tabPane.getTabs().contains(tab) 가드가 필요한 이유:
        //   드래그로 분리될 때 MOUSE_RELEASED → MOUSE_CLICKED 순으로 두 이벤트가 모두 발생한다.
        //   RELEASED에서 이미 탭이 제거됐는데 CLICKED까지 실행되면 두 번 분리를 시도하게 된다.
        //   탭이 이미 없는지 확인해서 중복 실행을 방지한다.
        headerLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tabPane.getTabs().contains(tab)) {
                detachTab(tab, title, e.getScreenX(), e.getScreenY());
                e.consume();
            }
        });
    }

    /**
     * 탭을 TabPane에서 제거하고 새 Stage(창)으로 띄운다.
     *
     * @param tab      분리할 탭
     * @param title    탭 제목 (새 창 타이틀에도 사용)
     * @param screenX  마우스를 뗀 화면 X 좌표 (새 창 위치 기준)
     * @param screenY  마우스를 뗀 화면 Y 좌표
     */
    private void detachTab(Tab tab, String title, double screenX, double screenY) {

        // 탭 컨텐츠를 탭에서 분리 (null로 설정해야 다른 Scene에 붙일 수 있음)
        Node content = tab.getContent();
        tab.setContent(null);

        // 원본 TabPane에서 탭 제거
        tabPane.getTabs().remove(tab);

        // ----------------------------------------------------------------
        // 새 창(Stage) 구성
        // ----------------------------------------------------------------

        // 상단 툴바: 합치기 버튼
        Button mergeBtn = new Button("◀ 탭으로 합치기");
        mergeBtn.setStyle("-fx-font-size: 12px;");

        HBox toolbar = new HBox(mergeBtn);
        toolbar.setStyle(
                        "-fx-padding: 6 10 6 10;" +
                        "-fx-background-color: #EEEEEE;" +
                        "-fx-border-color: #CCCCCC;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // BorderPane: 상단(툴바) + 가운데(탭 컨텐츠)
        BorderPane detachedRoot = new BorderPane();
        detachedRoot.setTop(toolbar);
        detachedRoot.setCenter(content);

        Stage detachedStage = new Stage();
        detachedStage.initOwner(primaryStage); // 메인 창의 자식 창으로 설정
        detachedStage.setTitle(title);
        detachedStage.setScene(new Scene(detachedRoot, 950, 700));

        // 마우스를 뗀 위치 근처에 창 배치
        detachedStage.setX(screenX - 100);
        detachedStage.setY(screenY - 30);

        // [탭으로 합치기] 버튼 클릭 → 원래 TabPane으로 복원
        mergeBtn.setOnAction(e ->
                attachTab(tab, title, content, detachedStage)
        );

        // 창 닫기(X) 버튼 → 탭으로 복원 (데이터 손실 방지)
        // e.consume()으로 기본 닫기를 막고, attachTab 안에서 직접 close() 호출
        detachedStage.setOnCloseRequest(e -> {
            e.consume();
            attachTab(tab, title, content, detachedStage);
        });

        detachedStage.show();
    }

    /**
     * 분리된 탭을 원래 TabPane으로 복원한다.
     *
     * @param tab           복원할 탭 객체
     * @param title         탭 제목 (Label graphic에 다시 설정)
     * @param content       탭에 다시 붙일 컨텐츠
     * @param detachedStage 닫을 분리 창
     */
    private void attachTab(Tab tab, String title, Node content, Stage detachedStage) {

        // 분리 창 닫기
        detachedStage.close();

        // 컨텐츠를 탭에 다시 연결
        tab.setContent(content);

        // 헤더 Label 텍스트 복원
        // instanceof 패턴 매칭 (Java 16+): 캐스팅 없이 바로 label 변수 사용 가능
        if (tab.getGraphic() instanceof Label label) {
            label.setText(title);
        }

        // 이미 목록에 없는 경우에만 추가 (중복 방지)
        if (!tabPane.getTabs().contains(tab)) {
            tabPane.getTabs().add(tab);
        }

        // 복원된 탭 선택
        tabPane.getSelectionModel().select(tab);
    }
}
