package net.slimediamond.espial.api;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.entity.Sign;

import java.util.List;

public final class SignText {

    private final Component front1;
    private final Component front2;
    private final Component front3;
    private final Component front4;

    private final Component back1;
    private final Component back2;
    private final Component back3;
    private final Component back4;

    private SignText(
            final Component front1,
            final Component front2,
            final Component front3,
            final Component front4,
            final Component back1,
            final Component back2,
            final Component back3,
            final Component back4) {
        this.front1 = front1;
        this.front2 = front2;
        this.front3 = front3;
        this.front4 = front4;

        this.back1 = back1;
        this.back2 = back2;
        this.back3 = back3;
        this.back4 = back4;
    }

    /**
     * Create a new wrapper for the four text lines on a sign.
     *
     * @param front1 Component for first line of the front
     * @param front2 Component for second line of the front
     * @param front3 Component for third line of the front
     * @param front4 Component for fourth line of the front
     * @param back1  Component for first line of the back
     * @param back2  Component for second line of the back
     * @param back3  Component for third line of the back
     * @param back4  Component for fourth line of the back
     * @return The corresponding <code>SignText</code>
     */
    public static SignText from(
            final Component front1,
            final Component front2,
            final Component front3,
            final Component front4,
            final Component back1,
            final Component back2,
            final Component back3,
            final Component back4) {
        return new SignText(front1, front2, front3, front4, back1, back2, back3, back4);
    }

    /**
     * Create a new wrapper for the four text lines on a sign.
     *
     * @param frontText The front text on the sign
     * @param backText The back text on the sign
     * @return The corresponding <code>SignText</code>
     */
    public static SignText from(final Sign.SignText frontText, final Sign.SignText backText) {
        final List<Component> front = frontText.lines().all();
        final List<Component> back = backText.lines().all();

        return new SignText(front.get(0), front.get(1), front.get(2), front.get(3),
                back.get(0), back.get(1), back.get(2), back.get(3));
    }

    /**
     * Create a new wrapper for the four text lines on a sign.
     *
     * @param front The front text on the sign
     * @param back The back text on the sign
     * @return The corresponding <code>SignText</code>
     */
    public static SignText from(final List<Component> front, final List<Component> back) {
        return new SignText(front.get(0), front.get(1), front.get(2), front.get(3),
                back.get(0), back.get(1), back.get(2), back.get(3));
    }

    public Component getFront1() {
        return front1;
    }

    public Component getFront2() {
        return front2;
    }

    public Component getFront3() {
        return front3;
    }

    public Component getFront4() {
        return front4;
    }

    public Component getBack1() {
        return back1;
    }

    public Component getBack2() {
        return back2;
    }

    public Component getBack3() {
        return back3;
    }

    public Component getBack4() {
        return back4;
    }

    public List<Component> getFront() {
        return List.of(front1, front2, front3, front4);
    }

    public List<Component> getBack() {
        return List.of(back1, back2, back3, back4);
    }

}
