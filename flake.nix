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
          default = (pkgs.mkShell.override { stdenv = pkgs.gcc14Stdenv; }) {
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
            # Keep CMake's setup hook from selecting a source directory on entry.
            dontUseCmakeConfigure = true;
          };
        });
    };
}
