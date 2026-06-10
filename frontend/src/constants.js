export const CAMERA_BRANDS = [
  { id: 'canon', label: 'CANON 佳能', logo: '/logo/canon.svg' },
  { id: 'nikon', label: 'NIKON 尼康', logo: '/logo/nikon.svg' },
  { id: 'fujifilm', label: 'FUJIFILM 富士', logo: '/logo/fujifilm.svg' },
  { id: 'pentax', label: 'PENTAX 宾得', logo: '/logo/pentax.svg' },
  { id: 'panasonic', label: 'PANASONIC 松下', logo: '/logo/panasonic.svg' },
  { id: 'hasselblad', label: 'HASSELBLAD 哈苏', logo: '/logo/hasselblad.svg' },
  { id: 'ricoh', label: 'RICOH 理光', logo: '/logo/ricoh.svg' },
  { id: 'contax', label: 'CONTAX 康泰时', logo: '/logo/contax.svg' },
  { id: 'phaseone', label: 'PHASE ONE 飞思', logo: '/logo/phaseone.svg' },
];

export const FILM_STOCKS = [
  { id: 'kodak-portra160', label: 'KODAK PORTRA 160' },
  { id: 'kodak-portra400', label: 'KODAK PORTRA 400' },
  { id: 'kodak-portra800', label: 'KODAK PORTRA 800' },
  { id: 'kodak-ektar100', label: 'KODAK EKTAR 100' },
  { id: 'kodak-gold200', label: 'KODAK GOLD 200' },
  { id: 'kodak-triX400', label: 'KODAK TRI-X 400' },
  { id: 'kodak-tmax400', label: 'KODAK T-MAX 400' },
  { id: 'kodak-colorplus200', label: 'KODAK COLORPLUS 200' },
  { id: 'kodak-ultramax400', label: 'KODAK ULTRAMAX 400' },
  { id: 'kodak-ektachrome100', label: 'KODAK EKTACHROME E100' },
  { id: 'fuji-provia100f', label: 'FUJIFILM PROVIA 100F' },
  { id: 'fuji-velvia50', label: 'FUJIFILM VELVIA 50' },
  { id: 'fuji-velvia100', label: 'FUJIFILM VELVIA 100' },
  { id: 'fuji-superia400', label: 'FUJIFILM SUPERIA X-TRA 400' },
  { id: 'fuji-acros100', label: 'FUJIFILM NEOPAN 100 ACROS' },
  { id: 'fuji-neopan400', label: 'FUJIFILM NEOPAN 400' },
  { id: 'ilford-hp5', label: 'ILFORD HP5 PLUS 400' },
  { id: 'ilford-fp4', label: 'ILFORD FP4 PLUS 125' },
  { id: 'ilford-delta100', label: 'ILFORD DELTA 100' },
  { id: 'ilford-delta400', label: 'ILFORD DELTA 400' },
  { id: 'ilford-delta3200', label: 'ILFORD DELTA 3200' },
  { id: 'ilford-panf', label: 'ILFORD PAN F PLUS 50' },
  { id: 'lomo100', label: 'LOMOGRAPHY 100' },
  { id: 'lomo400', label: 'LOMOGRAPHY 400' },
  { id: 'lomo800', label: 'LOMOGRAPHY 800' },
  { id: 'lomo-metropolis', label: 'LOMOGRAPHY METROPOLIS' },
  { id: 'lomo-purple', label: 'LOMOGRAPHY PURPLE' },
  { id: 'cinestill-50d', label: 'CINESTILL 50D' },
  { id: 'cinestill-800t', label: 'CINESTILL 800T' },
  { id: 'cinestill-bwxx', label: 'CINESTILL BWXX' },
  { id: 'rollei-infrared', label: 'ROLLEI INFRARED 400' },
  { id: 'rollei-retro400s', label: 'ROLLEI RETRO 400S' },
];

/** 品牌首字母颜色映射（用于 fallback 头像） */
export const BRAND_COLOR_MAP = {
  c: '333', n: '333', f: 'b64710', p: 'c9302b',
  h: '004d73', r: 'e60012', o: '005aff',
};

export function brandLabel(id) {
  const b = CAMERA_BRANDS.find(x => x.id === id);
  return b ? b.label : (id || '').toUpperCase();
}

export function brandEnLabel(id) {
  const l = brandLabel(id);
  return l ? l.split(/[\s一-鿿]+/)[0] || l : l;
}

export function filmLabel(id) {
  const f = FILM_STOCKS.find(x => x.id === id);
  return f ? f.label : (id || '').toUpperCase();
}

export function getBrandLogo(id) {
  const b = CAMERA_BRANDS.find(c => c.id === id);
  return b ? b.logo : '';
}

/** 图片占位 SVG（用于加载失败时回退） */
export const PLACEHOLDER_SVG =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 3'%3E%3Crect width='4' height='3' fill='%23f0f0f0'/%3E%3Ccircle cx='2' cy='1.2' r='0.4' fill='%23ddd'/%3E%3Cpath d='M0.5 2.8 L1.5 1.5 L2.5 2.2 L3.5 1 L4 2.8 Z' fill='%23ddd'/%3E%3C/svg%3E";

/**
 * 图片文件校验：魔数检查 + 大小限制（20MB）
 */
export const ALLOWED_MAGIC = [
  { magic: [0xFF, 0xD8, 0xFF], name: 'JPEG' },
  { magic: [0x89, 0x50, 0x4E, 0x47], name: 'PNG' },
  { magic: [0x42, 0x4D], name: 'BMP' },
  { magic: [0x49, 0x49, 0x2A, 0x00], name: 'TIFF' },
  { magic: [0x4D, 0x4D, 0x00, 0x2A], name: 'TIFF' },
];

export function readFileHeader(file, len) {
  return new Promise(r => {
    const fr = new FileReader();
    fr.onload = () => r(Array.from(new Uint8Array(fr.result)));
    fr.onerror = () => r([]);
    fr.readAsArrayBuffer(file.slice(0, len));
  });
}

export async function validateImageFile(file) {
  if (file.size > 20 * 1024 * 1024)
    return { ok: false, msg: `文件大小超过 20MB: ${file.name}` };
  const header = await readFileHeader(file, 8);
  const match = ALLOWED_MAGIC.some(t => t.magic.every((b, i) => header[i] === b));
  if (!match)
    return { ok: false, msg: `不支持的文件格式（仅 JPEG/PNG/BMP/TIFF）: ${file.name}` };
  return { ok: true };
}
