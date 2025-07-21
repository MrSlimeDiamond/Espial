package net.slimediamond.espial.sponge.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public class Permission {

    private final String permssionNode;

    private Permission(@NotNull final String permssionNode) {
        this.permssionNode = permssionNode;
    }

    /**
     * The {@link Permission} factory
     *
     * @param permissionNode Permission string
     * @return The created Permission
     * @throws IllegalArgumentException If the permission is not in the valid
     *                                  format
     */
    public static Permission of(@NotNull final String permissionNode) {
        checkPermissionName(permissionNode);
        return new Permission(permissionNode);
    }

    public String get() {
        return permssionNode;
    }

    @Override
    public String toString() {
        return get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Permission that = (Permission) o;
        return Objects.equals(permssionNode, that.permssionNode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(permssionNode);
    }

    /**
     * Ensures that the input is in the kebab case format, but also allows periods.
     *
     * @param s the input string
     * @throws IllegalArgumentException the exception to throw if fails
     */
    private static void checkPermissionName(final String s) throws IllegalArgumentException {
        if (!Pattern.compile("^[a-z\\-.]*$").matcher(s).matches()) {
            throw new IllegalArgumentException(
                    "Invalid permission id: \""
                            + s
                            + "\". Valid ids only contain characters 'a-z', '-', and '.'.");
        }
    }

}
