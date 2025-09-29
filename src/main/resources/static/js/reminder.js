(function() {
  async function checkReminders() {
    try {
      const res = await fetch('/reminders/due');
      if (!res.ok) return;

      const tasks = await res.json();
      if (tasks.length > 0) {
        tasks.forEach(task => {
          alert(`⏰ Reminder: "${task.title}" is due!`);
        });
      }
    } catch (err) {
      console.error("Reminder check failed", err);
    }
  }

  // check every 30 seconds
  setInterval(checkReminders, 30000);
})();
