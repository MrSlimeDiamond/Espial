package net.slimediamond.espial.sponge.query.selector;

import java.util.Optional;

public class DefaultRangeSelector extends RangeSelector {

    @Override
    public Optional<SelectorFlag> getFlag() {
        return Optional.empty();
    }

}
