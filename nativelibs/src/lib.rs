use std::ffi::{c_char, CStr};
use std::mem;

use resvg::tiny_skia::{Pixmap, Transform};
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

#[test]
fn view_box_sanity_test() {
    let svg = r##"
    <svg xmlns="http://www.w3.org/2000/svg" width="1920" height="1080">
    </svg>
    "##;
    assert_eq!(
        usvg::Tree::from_str(svg, &Options::default())
            .unwrap()
            .size()
            .width(),
        1920.0
    );
    assert_eq!(
        usvg::Tree::from_str(svg, &Options::default())
            .unwrap()
            .size()
            .height(),
        1080.0
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

type RenderTreeCall = extern "C" fn(u32, u32, *const u8);

#[no_mangle]
unsafe extern "C" fn render_tree(tree: *mut UsvgTree, draw_to_canvas: RenderTreeCall) {
    let tree = Box::from_raw(tree);

    let size = tree.0.size();
    let width = size.width() as u32;
    let height = size.height() as u32;

    let Some(mut pixmap) = Pixmap::new(width, height) else {
        eprintln!("failed to create pixmap");
        return;
    };

    resvg::render(&tree.0, Transform::identity(), &mut pixmap.as_mut());

    let mut buf = Vec::<u8>::with_capacity((width * height * 4) as usize);

    pixmap
        .data()
        .chunks(4)
        .map(|pix| {
            let [r, g, b, a] = pix.try_into().unwrap();
            [b, g, r, a]
        })
        .flatten()
        .for_each(|byte| {
            buf.push(byte);
        });

    draw_to_canvas(width, height, buf.as_ptr());

    mem::forget(tree);
}
