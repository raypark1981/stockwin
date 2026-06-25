package com.stockwin.stockwin;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX 앱의 진입점 클래스.
 *
 * Application을 상속받아 start()를 오버라이드하면
 * JavaFX 런타임이 이 메서드를 호출해서 창을 띄운다.
 * (main()은 Launcher.java에서 호출하고 있음)
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // FXMLLoader: fxml 파일을 읽어서 JavaFX 컴포넌트 트리로 변환하는 클래스
        // getResource()로 classpath 기준 파일 경로를 지정한다
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("hello-view.fxml")
        );

        // Scene: Stage 위에 올라가는 실제 UI 컨테이너
        // fxmlLoader.load()가 fxml을 파싱해서 루트 노드(BorderPane)를 반환한다
        // 크기를 생략하면 fxml에 지정한 prefWidth/prefHeight 값을 사용한다
        Scene scene = new Scene(fxmlLoader.load());

        // fxmlLoader.getController(): fxml이 생성한 컨트롤러 인스턴스를 가져온다
        // Stage는 initialize() 시점에 아직 없으므로, load() 이후에 별도로 전달한다
        HelloController controller = fxmlLoader.getController();
        controller.setStage(stage);

        // Stage: OS 창(window)을 나타내는 JavaFX 객체
        stage.setTitle("StockWin - 주식 반자동 매매 프로그램");
        stage.setScene(scene);

        // 창 최소 크기 제한: 탭 레이아웃이 너무 좁아지지 않도록 설정
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // 창을 화면에 표시
        stage.show();
    }
}
