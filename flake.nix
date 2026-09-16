{
  description = "progarch";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.11";

  outputs = { self, nixpkgs }:
  let
    system = "x86_64-linux";
    pkgs = nixpkgs.legacyPackages.${system};
  in
  {
    devShell.${system} = pkgs.mkShellNoCC {
      shellHook = ''
        export JDTLS_CONFIG='cmd = {"${pkgs.jdt-language-server}/bin/jdtls", "--jvm-arg=-javaagent:${pkgs.lombok}/share/java/lombok.jar"}'
      '';
      packages = with pkgs; [
        javaPackages.compiler.temurin-bin.jdk-21
        maven
      ];
    };
  };
}
