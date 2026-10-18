const inputs = document.querySelectorAll("input");
const errorBox = document.getElementById("error");

const valid = {
  2: /^[01]+$/,
  8: /^[0-7]+$/,
  10: /^[0-9]+$/,
  16: /^[0-9a-fA-F]+$/
};

function toBigInt(text, base) {
  let v = 0n;
  for (const ch of text.toLowerCase()) {
    v = v * BigInt(base) + BigInt(parseInt(ch, 16));
  }
  return v;
}

function convert(src) {
  const base = Number(src.dataset.base);
  const text = src.value.trim();

  inputs.forEach(i => i.classList.remove("invalid"));
  errorBox.textContent = "";

  if (text === "") {
    inputs.forEach(i => { if (i !== src) i.value = ""; });
    return;
  }

  if (!valid[base].test(text)) {
    src.classList.add("invalid");
    errorBox.textContent = "Invalid digit for base " + base;
    return;
  }

  const value = toBigInt(text, base);
  inputs.forEach(i => {
    if (i !== src) {
      i.value = value.toString(Number(i.dataset.base)).toUpperCase();
    }
  });
}

inputs.forEach(i => i.addEventListener("input", () => convert(i)));

document.getElementById("clear").onclick = () => {
  inputs.forEach(i => { i.value = ""; i.classList.remove("invalid"); });
  errorBox.textContent = "";
};