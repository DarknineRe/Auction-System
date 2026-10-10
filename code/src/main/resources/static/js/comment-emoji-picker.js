document.addEventListener("click", (event) => {
  if (!(event.target instanceof Element)) {
    return;
  }

  const button = event.target.closest("button[data-emoji]");
  const picker = button?.closest(".comment-emoji-picker");
  const textarea = picker?.closest(".field")?.querySelector("textarea");

  if (!button || !textarea) {
    return;
  }

  const emoji = button.dataset.emoji;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const nextValue = textarea.value.slice(0, start) + emoji + textarea.value.slice(end);

  if (textarea.maxLength >= 0 && nextValue.length > textarea.maxLength) {
    return;
  }

  textarea.value = nextValue;
  textarea.setSelectionRange(start + emoji.length, start + emoji.length);
  textarea.dispatchEvent(new Event("input", { bubbles: true }));
  textarea.focus();
});
