import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Startup splash screen for Grove.
 *
 * <p>Demonstrates concepts from CSC360 lectures:</p>
 * <ul>
 *   <li>FK §2.2–2.6  — 2D geometric objects: Rectangle and Line nodes
 *       positioned using coordinate arithmetic to draw a tree diagram.</li>
 *   <li>FK §2.7–2.12 — Animation and Interpolation: FadeTransition and
 *       ScaleTransition combine in a ParallelTransition for the entry
 *       animation; a Timeline with KeyValue + EASE_BOTH interpolation drives
 *       the progress bar; a second Timeline cycles the loading-text labels.</li>
 *   <li>JV §3        — JavaFX programmatic layout: VBox, Pane, and CSS
 *       inline styles without FXML or Scene Builder.</li>
 * </ul>
 *
 * <p>Usage (called once from App.start):</p>
 * <pre>
 *   new SplashScreen(() -> launchApp(primaryStage)).show();
 * </pre>
 */
public class SplashScreen {

    // ── Visual constants ──────────────────────────────────────────────────────
    private static final int    WIDTH            = 480;
    private static final int    HEIGHT           = 300;
    private static final double SPLASH_DURATION  = 3000; // ms — minimum display time
    private static final double ENTRY_DURATION   = 700;  // ms
    private static final double EXIT_DURATION    = 500;  // ms

    // ── Palette — mirrors dark-theme.css so the transition feels seamless ────
    private static final String BG        = "#18202F";
    private static final String CARD      = "#1E2840";
    private static final String BORDER    = "#2D3748";
    private static final String ACCENT    = "#B8734F";
    private static final String TEXT_PRI  = "#FAF7F2";
    private static final String TEXT_SEC  = "#8BA7C7";
    private static final String GREEN     = "#6BAF92";
    private static final String TRACK     = "#2D3748";

    // ── State ────────────────────────────────────────────────────────────────
    private final Stage      splashStage;
    private final Runnable   onComplete;
    private final ProgressBar progressBar = new ProgressBar(0);

    // ─────────────────────────────────────────────────────────────────────────

    public SplashScreen(Runnable onComplete) {
        this.onComplete  = onComplete;
        this.splashStage = new Stage(StageStyle.TRANSPARENT);
        this.splashStage.setAlwaysOnTop(true);
    }

    /** Builds the scene, shows the stage, and starts all animations. */
    public void show() {
        VBox root = buildLayout();

        // DropShadow gives the card a floating appearance on any desktop background
        root.setEffect(new DropShadow(24, Color.rgb(0, 0, 0, 0.55)));

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.TRANSPARENT);   // let StageStyle.TRANSPARENT show through

        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();

        animateIn(root);   // FK §2.7 — entry transition
        startTimer();      // FK §2.7 — progress + text cycle + exit trigger
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    private VBox buildLayout() {
        // ── Tree graphic (FK §2.2 — 2D geometric objects) ───────────────────
        Pane treeIcon = buildTreeIcon();

        // ── Text labels ──────────────────────────────────────────────────────
        Label title = new Label("Grove");
        title.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + ACCENT + ";" +
            "-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;"
        );

        Label tagline = new Label("A tree-structured object editor");
        tagline.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + TEXT_PRI + ";" +
            "-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;"
        );

        Label course = new Label("CSC360  ·  Computer Graphics and Digital Image Processing");
        course.setStyle(labelStyle(TEXT_SEC, "11px"));

        Label team = new Label("Group 7  ·  Ahmedabad University  ·  Monsoon 2026");
        team.setStyle(labelStyle(TEXT_SEC, "11px"));

        // ── Progress bar (FK §2.7 — interpolated KeyValue animation) ─────────
        progressBar.setPrefWidth(340);
        progressBar.setPrefHeight(4);
        progressBar.setStyle(
            "-fx-accent: "            + GREEN + ";" +
            "-fx-background-color: "  + TRACK + ";" +
            "-fx-background-radius: 2px;" +
            "-fx-pref-height: 4px;"
        );

        // ── Cycling loading-text label ────────────────────────────────────────
        Label loadingText = new Label("Initialising…");
        loadingText.setStyle(labelStyle("#4A5A6A", "10px"));

        // ── Divider ──────────────────────────────────────────────────────────
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + "; -fx-pref-width: 340px;");
        sep.setMaxWidth(340);

        // ── Compose top section (icon + text) ────────────────────────────────
        VBox textBlock = new VBox(4, title, tagline, course, team);
        textBlock.setAlignment(Pos.CENTER);

        VBox topSection = new VBox(14, treeIcon, textBlock);
        topSection.setAlignment(Pos.CENTER);

        // ── Compose bottom section (progress + label) ─────────────────────────
        VBox bottomSection = new VBox(6, progressBar, loadingText);
        bottomSection.setAlignment(Pos.CENTER);

        // ── Root card ─────────────────────────────────────────────────────────
        VBox root = new VBox(18, topSection, sep, bottomSection);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32, 40, 28, 40));
        root.setPrefSize(WIDTH, HEIGHT);
        root.setStyle(
            "-fx-background-color: " + CARD   + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: "      + BORDER + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );

        // Cycle loading messages alongside the progress animation
        animateLoadingText(loadingText);

        return root;
    }

    /**
     * Draws a small tree diagram using Rectangle and Line nodes.
     *
     * <p>FK §2.2–2.6 — Geometric Objects: each rectangle is defined by its
     * top-left corner (x, y), width, and height — the same coordinate model
     * used in the midpoint line-drawing algorithm. The connecting lines are
     * two-point line segments, consistent with FK §3.2.</p>
     */
    private Pane buildTreeIcon() {
        // ── Colour palette for nodes ─────────────────────────────────────────
        Color rootColor   = Color.web(ACCENT);     // terracotta — root
        Color childColor  = Color.web(GREEN);      // green — first-level children
        Color grandColor  = Color.web(TEXT_SEC);   // blue-grey — grandchild
        Color lineColor   = Color.web(BORDER);

        // ── Root node (centred at x=55) ──────────────────────────────────────
        Rectangle rootRect = rect(40, 2, 30, 16, rootColor);

        // ── Connecting lines (from root bottom-centre: 55, 18) ───────────────
        Line lineLeft  = line(55, 18, 20, 40, lineColor);   // to left child
        Line lineRight = line(55, 18, 88, 40, lineColor);   // to right child

        // ── First-level children ──────────────────────────────────────────────
        Rectangle leftChild  = rect( 7, 40, 26, 14, childColor);  // centre: 20, 47
        Rectangle rightChild = rect(75, 40, 26, 14, childColor);  // centre: 88, 47

        // ── Grandchild under rightChild (centre bottom: 88, 54) ───────────────
        Line lineGrand = line(88, 54, 88, 64, lineColor);
        Rectangle grandChild = rect(76, 64, 24, 13, grandColor);   // centre: 88, 70

        Pane pane = new Pane(
            lineLeft, lineRight, lineGrand,
            rootRect, leftChild, rightChild, grandChild
        );
        pane.setPrefSize(115, 80);
        pane.setMaxSize(115, 80);
        return pane;
    }

    // ── Animations ────────────────────────────────────────────────────────────

    /**
     * Entry animation: FadeTransition + ScaleTransition run in parallel.
     *
     * <p>FK §2.7 — Animation: opacity interpolation (fade) and uniform
     * scale interpolation (zoom-in) are applied simultaneously using
     * ParallelTransition. EASE_OUT mirrors the deceleration curve described
     * in FK §2.12 for smooth motion perception.</p>
     */
    private void animateIn(VBox root) {
        root.setOpacity(0);
        root.setScaleX(0.92);
        root.setScaleY(0.92);

        // FK §2.7 — linear opacity interpolation from 0 → 1
        FadeTransition fade = new FadeTransition(Duration.millis(ENTRY_DURATION), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        // FK §2.12 — scale interpolation with EASE_OUT deceleration
        ScaleTransition scale = new ScaleTransition(Duration.millis(ENTRY_DURATION), root);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, scale).play();
    }

    /**
     * Cycles the loading-text label through four messages using a Timeline.
     *
     * <p>FK §2.7 — Animation: each KeyFrame fires a discrete event at a
     * scheduled time, mirroring how frame-by-frame animation is described
     * in FK §2.9 (discrete vs. continuous motion).</p>
     */
    private void animateLoadingText(Label label) {
        String[] messages = {
            "Initialising…",
            "Loading tree data…",
            "Building interface…",
            "Almost ready…"
        };

        Timeline cycle = new Timeline();
        for (int i = 0; i < messages.length; i++) {
            final String msg = messages[i];
            // Space messages evenly across the splash duration (leaving the last one visible)
            double offset = i * (SPLASH_DURATION / (messages.length + 1));
            cycle.getKeyFrames().add(new KeyFrame(Duration.millis(offset), e -> label.setText(msg)));
        }
        cycle.play();
    }

    /**
     * Starts the splash timer: fills the progress bar over {@code SPLASH_DURATION}
     * then triggers the exit animation.
     *
     * <p>FK §2.7–2.12 — Interpolation: KeyValue with {@code EASE_BOTH} produces
     * a slow-start / slow-end easing identical to the cubic Bézier curves used
     * for smooth animation described in FK §2.11. The interpolator maps elapsed
     * time t ∈ [0,1] to a non-linear progress value, so the bar accelerates
     * mid-fill and decelerates at both ends.</p>
     */
    private void startTimer() {
        Timeline timer = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(progressBar.progressProperty(), 0.0)
            ),
            new KeyFrame(Duration.millis(SPLASH_DURATION),
                new KeyValue(progressBar.progressProperty(), 1.0, Interpolator.EASE_BOTH)
            )
        );
        timer.setOnFinished(e -> animateOut());
        timer.play();
    }

    /**
     * Exit animation: fades the card to transparent, then calls {@code onComplete}.
     *
     * <p>FK §2.7 — Animation: FadeTransition interpolates opacity from 1 → 0,
     * mirroring the entry fade in reverse. The {@code setOnFinished} callback
     * is the equivalent of a post-animation hook described in FK §2.9.</p>
     */
    private void animateOut() {
        VBox root = (VBox) splashStage.getScene().getRoot();

        FadeTransition fade = new FadeTransition(Duration.millis(EXIT_DURATION), root);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            splashStage.hide();
            onComplete.run();        // App.launchApp() runs here on the FX thread
        });
        fade.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Creates a rounded Rectangle with the given geometry and fill colour. */
    private static Rectangle rect(double x, double y, double w, double h, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(4);
        r.setArcHeight(4);
        r.setFill(fill);
        return r;
    }

    /** Creates a Line between two coordinate pairs with the given stroke colour. */
    private static Line line(double x1, double y1, double x2, double y2, Color stroke) {
        Line l = new Line(x1, y1, x2, y2);
        l.setStroke(stroke);
        l.setStrokeWidth(1.5);
        return l;
    }

    /** Shared inline-style string for secondary text labels. */
    private static String labelStyle(String color, String size) {
        return  "-fx-font-size: "   + size  + ";" +
                "-fx-text-fill: "   + color + ";" +
                "-fx-font-family: 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;";
    }
}
