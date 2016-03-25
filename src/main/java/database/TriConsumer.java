package database;

import java.sql.SQLException;
import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<X, T, U> {
    void accept(X var0, T var1, U var2);
}
