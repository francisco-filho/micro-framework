package util;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<X, T, U, R> {
    R apply(X var0, T var1, U var2);
}
