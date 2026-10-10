// Reload the payment page once the QR code shown to the buyer has been paid.
const qr = document.querySelector(".paybox__qr[data-status-url]");
if (qr) {
  const timer = setInterval(async () => {
    try {
      const response = await fetch(qr.dataset.statusUrl, { headers: { Accept: "application/json" } });
      if (!response.ok) return;
      const payment = await response.json();
      if (payment.status !== "AWAITING_PAYMENT") {
        clearInterval(timer);
        location.reload();
      }
    } catch {
      // Network hiccup: try again on the next tick.
    }
  }, 5000);
}
