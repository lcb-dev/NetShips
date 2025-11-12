package casey.lcbdev.model;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

class FxTestBase {

    @BeforeAll
    static void initToolkit() {
        Platform.startup(() -> {});
    }
}