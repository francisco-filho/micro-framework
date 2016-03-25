package util;

import java.sql.SQLException;
import java.util.Objects;

@FunctionalInterface
public interface DatabaseConsumer<T> {
    void accept(T var1) throws SQLException;

    default DatabaseConsumer<T> andThen(DatabaseConsumer<? super T> var1) throws SQLException{
        Objects.requireNonNull(var1);
        return (var2) -> {
            this.accept(var2);
            var1.accept(var2);
        };
    }
}
