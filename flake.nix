{
  description = "A very basic flake";

  outputs = { self, nixpkgs }:
    let
      pkgs = nixpkgs.legacyPackages.x86_64-linux.pkgs;
      openjdk = pkgs.openjdk19.override { enableJavaFX = true; };
    in {
      devShells.x86_64-linux.default = with pkgs; mkShell {
        buildInputs = [ openjdk python3 ];
        };
  };
}
