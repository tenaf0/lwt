{
  description = "A very basic flake";

  outputs = { self, nixpkgs }:
    let
      pkgs = nixpkgs.legacyPackages.x86_64-linux.pkgs;
      openjdk = pkgs.openjdk.override { enableJavaFX = true; };
    in {
      devShells.x86_64-linux.default = with pkgs; mkShell {
        shellHook = ''
          export LD_LIBRARY_PATH=/home/florian/git/lwt/udpipe/udpipe-1.3.0-bin/bin-linux64/java/:$LD_LIBRARY_PATH
        '';
        buildInputs = [ openjdk ];
        };
  };
}
