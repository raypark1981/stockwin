module com.stockwin.stockwin {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.stockwin.stockwin to javafx.fxml;
    exports com.stockwin.stockwin;
}