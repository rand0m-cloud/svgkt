use std::ffi::{c_char, CStr};
use std::mem;

use resvg::usvg;
use resvg::usvg::Options;

#[test]
fn bounding_box_sanity_test() {
    let svg = r##"
    <svg xmlns="http://www.w3.org/2000/svg">
      <rect width="100" height="100"> </rect>
    </svg>
    "##;
    assert_eq!(
        usvg::Tree::from_str(svg, &Options::default())
            .unwrap()
            .size()
            .width(),
        100.0
    );
    assert_eq!(
        usvg::Tree::from_str(svg, &Options::default())
            .unwrap()
            .size()
            .height(),
        100.0
    );
}

struct UsvgTree(usvg::Tree);

#[no_mangle]
unsafe extern "C" fn read_svg_to_tree(utf8_string: *const c_char) -> *mut UsvgTree {
    let go = || {
        let str = CStr::from_ptr(utf8_string)
            .to_str()
            .map_err(|e| eprintln!("failed to make utf8 string: {e}"))
            .ok()?;
        let tree = Box::new(UsvgTree(
            usvg::Tree::from_str(str, &Options::default())
                .map_err(|e| eprintln!("tree failed to create: {e}"))
                .ok()?,
        ));

        Some(Box::into_raw(tree))
    };
    go().unwrap_or_else(|| std::ptr::null_mut())
}

#[repr(C)]
struct BoundingBox {
    width: f32,
    height: f32,
}

#[no_mangle]
unsafe extern "C" fn get_bounding_box(tree: *mut UsvgTree) -> BoundingBox {
    let tree = Box::from_raw(tree);
    let size = tree.0.size();
    let bb = BoundingBox {
        width: size.width(),
        height: size.height(),
    };

    mem::forget(tree);

    bb
}

#[no_mangle]
unsafe extern "C" fn free_tree(tree: *mut UsvgTree) {
    drop(Box::from_raw(tree))
}
