package android.content;
import java.util.function.Consumer;
// Desktop-mirror shim (internal QA tooling only). The host (MirrorMain)
// registers hooks so game-level intents have somewhere honest to go.
public class MirrorHooks {
    public static Consumer<Intent> activityLauncher;
    public static Consumer<String> toaster;
}
