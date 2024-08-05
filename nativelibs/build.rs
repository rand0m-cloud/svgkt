use std::env;
use std::fs::{create_dir, remove_dir_all};
use std::io::ErrorKind::NotFound;
use std::process::{Command, Stdio};

use cbindgen::Config;

fn main() {
    println!("cargo:rerun-if-changed=build.rs");
    let crate_dir = env::var("CARGO_MANIFEST_DIR").unwrap();

    cbindgen::Builder::new()
        .with_crate(crate_dir)
        .with_config(Config::from_file("cbindgen.toml").unwrap())
        .generate()
        .map_or_else(
            |error| match error {
                cbindgen::Error::ParseSyntaxError { .. } => std::process::exit(0),
                e => panic!("{:?}", e),
            },
            |bindings| {
                bindings.write_to_file("target/include/bindings.h");
            },
        );

    let _ = remove_dir_all("target/java");
    let _ = create_dir("target/java");

    assert!(
        Command::new("jextract")
            .args(&[
                "-t",
                "org.svgkt.nativelibs.sys",
                "../include/bindings.h",
            ])
            .current_dir("target/java")
            .stderr(Stdio::inherit())
            .stdout(Stdio::inherit())
            .spawn()
            .map_err(|e| {
                if e.kind() == NotFound {
                    println!("cargo::warning=missing jextract command to build java bindings");
                    println!("cargo::warning={:?}", std::env::var("PATH"));
                    std::process::abort()
                } else {
                    e
                }
            })
            .and_then(|mut cmd| cmd.wait())
            .expect("jextract command to work and generate java bindings")
            .success(),
        "failed to generate java bindings"
    );
}
