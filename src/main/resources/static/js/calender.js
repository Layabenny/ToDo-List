// Calendar functionality for Productivity Pro Todo App
class TodoCalendar {
    constructor() {
        this.currentDate = new Date();
        this.selectedDate = new Date();
        this.tasks = [];
        this.init();
    }

    init() {
        this.renderCalendar();
        this.attachEventListeners();
        this.loadTasks();
    }

    // Render the calendar
    renderCalendar() {
        const calendarElement = document.querySelector('.mini-calendar');
        if (!calendarElement) return;

        const monthYear = this.currentDate.toLocaleDateString('en-US', { 
            month: 'long', 
            year: 'numeric' 
        });

        const calendarHTML = `
            <div class="calendar-header">
                <h3>${monthYear}</h3>
                <div class="calendar-nav">
                    <button class="calendar-prev" title="Previous Month">←</button>
                    <button class="calendar-next" title="Next Month">→</button>
                </div>
            </div>
            <div class="calendar-weekdays">
                ${['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
                    .map(day => `<div class="calendar-weekday">${day}</div>`)
                    .join('')}
            </div>
            <div class="calendar-days">
                ${this.generateCalendarDays()}
            </div>
            <div class="calendar-events">
                <h4>Today's Tasks</h4>
                <div class="calendar-event-list">
                    ${this.renderTodaysEvents()}
                </div>
            </div>
        `;

        calendarElement.innerHTML = calendarHTML;
        this.highlightSelectedDate();
    }

    // Generate calendar days grid
    generateCalendarDays() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDay = firstDay.getDay();

        let daysHTML = '';

        // Previous month days
        const prevMonthLastDay = new Date(year, month, 0).getDate();
        for (let i = startingDay - 1; i >= 0; i--) {
            const day = prevMonthLastDay - i;
            daysHTML += `<div class="calendar-day other-month">${day}</div>`;
        }

        // Current month days
        const today = new Date();
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const isToday = this.isSameDate(date, today);
            const isSelected = this.isSameDate(date, this.selectedDate);
            const hasTasks = this.hasTasksOnDate(date);
            
            let dayClass = 'calendar-day';
            if (isToday) dayClass += ' today';
            if (isSelected) dayClass += ' selected';
            if (hasTasks) dayClass += ' has-tasks';

            daysHTML += `<div class="${dayClass}" data-date="${date.toISOString()}">${day}</div>`;
        }

        // Next month days
        const totalCells = 42; // 6 weeks
        const remainingCells = totalCells - (startingDay + daysInMonth);
        for (let day = 1; day <= remainingCells; day++) {
            daysHTML += `<div class="calendar-day other-month">${day}</div>`;
        }

        return daysHTML;
    }

    // Render today's events
    renderTodaysEvents() {
        const today = new Date();
        const todaysTasks = this.getTasksForDate(today);

        if (todaysTasks.length === 0) {
            return '<div class="calendar-empty">No tasks for today</div>';
        }

        return todaysTasks.slice(0, 3).map(task => `
            <div class="calendar-event ${task.completed ? 'completed' : ''} ${this.isTaskUrgent(task) ? 'urgent' : ''}" 
                 data-task-id="${task.id}">
                <div class="calendar-event-title">${this.escapeHtml(task.title)}</div>
                ${task.dueDate ? `
                    <div class="calendar-event-time">
                        ${new Date(task.dueDate).toLocaleTimeString('en-US', { 
                            hour: '2-digit', 
                            minute: '2-digit' 
                        })}
                    </div>
                ` : ''}
            </div>
        `).join('');
    }

    // Attach event listeners
    attachEventListeners() {
        document.addEventListener('click', (e) => {
            // Calendar navigation
            if (e.target.classList.contains('calendar-prev')) {
                this.previousMonth();
            } else if (e.target.classList.contains('calendar-next')) {
                this.nextMonth();
            }
            
            // Day selection
            else if (e.target.classList.contains('calendar-day') && !e.target.classList.contains('other-month')) {
                this.selectDate(e.target.dataset.date);
            }
            
            // Event click
            else if (e.target.closest('.calendar-event')) {
                const eventElement = e.target.closest('.calendar-event');
                this.showEventDetails(eventElement.dataset.taskId);
            }
            
            // Modal close
            else if (e.target.classList.contains('calendar-modal-close') || 
                     e.target.classList.contains('calendar-modal')) {
                this.hideModal();
            }
            
            // Modal actions
            else if (e.target.classList.contains('calendar-modal-event-action')) {
                const action = e.target.classList.contains('complete') ? 'complete' : 'delete';
                const taskId = e.target.closest('.calendar-modal-event').dataset.taskId;
                this.handleTaskAction(taskId, action);
            }
        });

        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.hideModal();
            }
        });
    }

    // Navigation methods
    previousMonth() {
        this.currentDate.setMonth(this.currentDate.getMonth() - 1);
        this.renderCalendar();
    }

    nextMonth() {
        this.currentDate.setMonth(this.currentDate.getMonth() + 1);
        this.renderCalendar();
    }

    // Date selection
    selectDate(dateString) {
        this.selectedDate = new Date(dateString);
        this.renderCalendar();
        this.showDateEventsModal();
    }

    // Highlight selected date
    highlightSelectedDate() {
        const days = document.querySelectorAll('.calendar-day');
        days.forEach(day => {
            if (day.dataset.date) {
                const dayDate = new Date(day.dataset.date);
                if (this.isSameDate(dayDate, this.selectedDate)) {
                    day.classList.add('selected');
                }
            }
        });
    }

    // Task management
    loadTasks() {
        // This would typically fetch tasks from your backend
        // For now, we'll use a mock implementation
        this.tasks = this.getTasksFromDOM() || [];
    }

    getTasksFromDOM() {
        try {
            const taskElements = document.querySelectorAll('.task-card');
            return Array.from(taskElements).map(element => {
                const title = element.querySelector('h4').textContent;
                const description = element.querySelector('p').textContent;
                const completed = element.classList.contains('completed');
                
                // Extract due date from task meta
                const dueDateText = element.querySelector('.task-meta span')?.textContent;
                let dueDate = null;
                if (dueDateText && dueDateText.includes('Due:')) {
                    dueDate = this.parseDateFromText(dueDateText.replace('Due:', '').trim());
                }
                
                // Extract ID from forms
                const completeForm = element.querySelector('form[action*="/complete"]');
                const id = completeForm ? this.extractTaskId(completeForm.action) : Date.now();
                
                return {
                    id,
                    title,
                    description,
                    completed,
                    dueDate
                };
            });
        } catch (error) {
            console.error('Error loading tasks from DOM:', error);
            return [];
        }
    }

    extractTaskId(url) {
        const match = url.match(/\/tasks\/(\d+)\/complete/);
        return match ? parseInt(match[1]) : Date.now();
    }

    parseDateFromText(text) {
        try {
            return new Date(text);
        } catch (error) {
            return null;
        }
    }

    // Utility methods
    isSameDate(date1, date2) {
        return date1.toDateString() === date2.toDateString();
    }

    hasTasksOnDate(date) {
        return this.getTasksForDate(date).length > 0;
    }

    getTasksForDate(date) {
        return this.tasks.filter(task => {
            if (!task.dueDate) return false;
            const taskDate = new Date(task.dueDate);
            return this.isSameDate(taskDate, date);
        });
    }

    isTaskUrgent(task) {
        if (!task.dueDate || task.completed) return false;
        const dueDate = new Date(task.dueDate);
        const now = new Date();
        const timeDiff = dueDate.getTime() - now.getTime();
        const daysDiff = timeDiff / (1000 * 3600 * 24);
        return daysDiff <= 1; // Urgent if due within 24 hours
    }

    // Modal methods
    showEventDetails(taskId) {
        const task = this.tasks.find(t => t.id == taskId);
        if (!task) return;

        const modalHTML = `
            <div class="calendar-modal" style="display: flex;">
                <div class="calendar-modal-content">
                    <div class="calendar-modal-header">
                        <h3>Task Details</h3>
                        <button class="calendar-modal-close">&times;</button>
                    </div>
                    <div class="calendar-modal-event ${task.completed ? 'completed' : ''} ${this.isTaskUrgent(task) ? 'urgent' : ''}" 
                         data-task-id="${task.id}">
                        <div class="calendar-modal-event-header">
                            <div class="calendar-modal-event-title">${this.escapeHtml(task.title)}</div>
                            <div class="calendar-modal-event-actions">
                                <button class="calendar-modal-event-action complete" title="Mark as ${task.completed ? 'incomplete' : 'complete'}">
                                    ${task.completed ? '↶' : '✓'}
                                </button>
                                <button class="calendar-modal-event-action delete" title="Delete task">🗑️</button>
                            </div>
                        </div>
                        <div class="calendar-modal-event-details">
                            ${this.escapeHtml(task.description || 'No description')}
                        </div>
                        ${task.dueDate ? `
                            <div class="calendar-modal-event-time">
                                <span><strong>Due:</strong> ${new Date(task.dueDate).toLocaleString()}</span>
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal
        const existingModal = document.querySelector('.calendar-modal');
        if (existingModal) {
            existingModal.remove();
        }

        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    showDateEventsModal() {
        const dateTasks = this.getTasksForDate(this.selectedDate);
        const dateString = this.selectedDate.toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });

        const modalHTML = `
            <div class="calendar-modal" style="display: flex;">
                <div class="calendar-modal-content">
                    <div class="calendar-modal-header">
                        <h3>Tasks for ${dateString}</h3>
                        <button class="calendar-modal-close">&times;</button>
                    </div>
                    <div class="calendar-modal-events">
                        ${dateTasks.length > 0 ? 
                            dateTasks.map(task => `
                                <div class="calendar-modal-event ${task.completed ? 'completed' : ''} ${this.isTaskUrgent(task) ? 'urgent' : ''}" 
                                     data-task-id="${task.id}">
                                    <div class="calendar-modal-event-header">
                                        <div class="calendar-modal-event-title">${this.escapeHtml(task.title)}</div>
                                        <div class="calendar-modal-event-actions">
                                            <button class="calendar-modal-event-action complete" title="Mark as ${task.completed ? 'incomplete' : 'complete'}">
                                                ${task.completed ? '↶' : '✓'}
                                            </button>
                                            <button class="calendar-modal-event-action delete" title="Delete task">🗑️</button>
                                        </div>
                                    </div>
                                    <div class="calendar-modal-event-details">
                                        ${this.escapeHtml(task.description || 'No description')}
                                    </div>
                                    ${task.dueDate ? `
                                        <div class="calendar-modal-event-time">
                                            <span><strong>Due:</strong> ${new Date(task.dueDate).toLocaleTimeString('en-US', { 
                                                hour: '2-digit', 
                                                minute: '2-digit' 
                                            })}</span>
                                        </div>
                                    ` : ''}
                                </div>
                            `).join('') :
                            '<div class="calendar-empty">No tasks for this date</div>'
                        }
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal
        const existingModal = document.querySelector('.calendar-modal');
        if (existingModal) {
            existingModal.remove();
        }

        document.body.insertAdjacentHTML('beforeend', modalHTML);
    }

    hideModal() {
        const modal = document.querySelector('.calendar-modal');
        if (modal) {
            modal.style.animation = 'modalFadeIn 0.3s ease reverse';
            setTimeout(() => modal.remove(), 300);
        }
    }

    // Task actions
    handleTaskAction(taskId, action) {
        const taskElement = document.querySelector(`[data-task-id="${taskId}"]`);
        
        if (action === 'complete') {
            this.toggleTaskCompletion(taskId);
        } else if (action === 'delete') {
            this.deleteTask(taskId);
        }
        
        // Refresh calendar and close modal
        this.renderCalendar();
        this.hideModal();
        
        // Trigger form submission for actual task updates
        this.submitTaskAction(taskId, action);
    }

    toggleTaskCompletion(taskId) {
        const task = this.tasks.find(t => t.id == taskId);
        if (task) {
            task.completed = !task.completed;
        }
    }

    deleteTask(taskId) {
        this.tasks = this.tasks.filter(t => t.id != taskId);
    }

    submitTaskAction(taskId, action) {
        // Find and submit the corresponding form
        const formSelector = action === 'complete' ? 
            `form[action*="/tasks/${taskId}/complete"]` :
            `form[action*="/tasks/${taskId}/delete"]`;
            
        const form = document.querySelector(formSelector);
        if (form) {
            form.submit();
        }
    }

    // Utility method to escape HTML
    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // Public method to refresh calendar
    refresh() {
        this.loadTasks();
        this.renderCalendar();
    }
}

// Initialize calendar when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.todoCalendar = new TodoCalendar();
    
    // Refresh calendar when tasks are updated
    const observer = new MutationObserver(function() {
        if (window.todoCalendar) {
            window.todoCalendar.refresh();
        }
    });
    
    const tasksContainer = document.querySelector('.tasks-grid');
    if (tasksContainer) {
        observer.observe(tasksContainer, { childList: true, subtree: true });
    }
});

// Export for global access
if (typeof module !== 'undefined' && module.exports) {
    module.exports = TodoCalendar;
}