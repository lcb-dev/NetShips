package casey.lcbdev.model.board;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

class FxTestBase {

    @BeforeAll
    static void initToolkit() {
        Platform.startup(() -> {});
    }
}