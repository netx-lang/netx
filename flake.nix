{
  description = "NetX development environment";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";

  outputs = { nixpkgs, ... }:
    let
      # macOS needs separate validation of Clang, OpenMP and LTO.
      systems = [ "x86_64-linux" "aarch64-linux" ];
      forAllSystems = nixpkgs.lib.genAttrs systems;
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs { inherit system; };
        in
        {
          default = pkgs.mkShell {
            packages = with pkgs; [
              jdk25
              git
            ];

            nativeBuildInputs = with pkgs; [
              cmake
              ninja
              gnumake
            ];

            buildInputs = with pkgs; [
              boost
              nlohmann_json
              gtest
            ];

            JAVA_HOME = "${pkgs.jdk25}";
            # The gcc wrapper appends -D_FORTIFY_SOURCE=2 after all user flags, which
            # glibc flags as a #warning under -O0 (Debug builds with sanitizers), and
            # -Werror turns that into a hard error. Disable it for local dev builds.
            hardeningDisable = [ "fortify" ];
            # Keep CMake's setup hook from selecting a source directory on entry.
            dontUseCmakeConfigure = true;
          };
        });
    };
}
