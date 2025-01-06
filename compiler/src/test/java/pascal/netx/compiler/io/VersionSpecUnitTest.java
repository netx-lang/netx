package pascal.netx.compiler.io;

import org.junit.jupiter.api.Test;
import pascal.netx.compiler.model.scope.ProjectScope;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionSpecUnitTest {
    @Test
    void testExact() {
        var spec = ProjectConfigReader.VersionSpec.of("1.2.3");
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.2.3")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("1.2.4")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("1.2.2")));
    }

    @Test
    void testGreater() {
        var spec = ProjectConfigReader.VersionSpec.of(">1.2.3");
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.2.4")));
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("2.1.0")));
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.5.1")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("1.2.3")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("1.2.2")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("0.8.12")));
    }

    @Test
    void testGreaterEqual() {
        var spec = ProjectConfigReader.VersionSpec.of(">=1.2.3");
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.2.3")));
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.2.4")));
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("2.1.0")));
        assertTrue(spec.isCompatibleWith(ProjectScope.Version.of("1.5.1")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("1.2.2")));
        assertFalse(spec.isCompatibleWith(ProjectScope.Version.of("0.8.12")));
    }
}
