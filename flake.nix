{
  description = "BodyCheck - Body composition QR code scanner";
  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixpkgs-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import "${nixpkgs}" {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };

        gradle = pkgs.stdenv.mkDerivation rec {
          pname = "gradle";
          version = "9.5.0";
          src = pkgs.fetchurl {
            url = "https://services.gradle.org/distributions/gradle-${version}-bin.zip";
            sha256 = "sha256-VTx49Q2vzVTWW5pERkkFeFdGnt+DZDE4lpVghTbWt0Y=";
          };
          nativeBuildInputs = [ pkgs.unzip pkgs.makeWrapper ];
          unpackPhase = "unzip $src";
          installPhase = ''
            mkdir -p $out
            cp -r gradle-${version}/* $out/
            wrapProgram $out/bin/gradle \
              --set JAVA_HOME "${pkgs.jdk21}/lib/openjdk"
          '';
        };

        android = pkgs.androidenv.composeAndroidPackages {
          buildToolsVersions = [ "36.0.0" ];
          platformVersions = [ "36" ];
          abiVersions = [ "armeabi-v7a" "arm64-v8a" ];
        };
      in
      {
        devShells.default = pkgs.mkShell {
          packages = [
            pkgs.jdk21
            gradle
            pkgs.android-tools
            android.androidsdk
          ];
          ANDROID_SDK_ROOT = "${android.androidsdk}/libexec/android-sdk";
          ANDROID_HOME = "${android.androidsdk}/libexec/android-sdk";
          JAVA_HOME = "${pkgs.jdk21}/lib/openjdk";
        };
      }
    );
}
