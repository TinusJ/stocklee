/**
 * Dashboard JavaScript functionality
 * Contains buy/sell stock operations and real-time updates
 */

// Global variables
let currentMaxShares = 0;
let stompClient = null;

/**
 * Initialize dashboard functionality when DOM is ready
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

/**
 * Initialize all dashboard functionality
 */
function initializeDashboard() {
    // Initialize sell button event handlers
    initializeSellButtons();
    
    // Initialize buy stock form handlers
    initializeBuyStockHandlers();
    
    // Initialize WebSocket connection if libraries are available
    if (typeof SockJS !== 'undefined' && typeof StompJs !== 'undefined') {
        connectWebSocket();
    } else {
        console.warn('WebSocket libraries not available. Real-time updates disabled.');
        updateWebSocketStatus('disconnected');
    }
}

/**
 * Initialize sell button event handlers
 */
function initializeSellButtons() {
    const sellButtons = document.querySelectorAll('.sell-stock-btn');
    sellButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            const stockId = this.getAttribute('data-stock-id');
            const stockSymbol = this.getAttribute('data-stock-symbol');
            const quantity = this.getAttribute('data-stock-quantity');
            prepareSell(stockId, stockSymbol, quantity);
        });
    });
}

/**
 * Initialize buy stock form handlers
 */
function initializeBuyStockHandlers() {
    // Auto-uppercase stock symbol input
    const symbolInput = document.getElementById('symbol');
    if (symbolInput) {
        symbolInput.addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase();
            // Clear purchase price when symbol changes
            const purchasePriceInput = document.getElementById('purchasePrice');
            const currentPriceDisplay = document.getElementById('currentPriceDisplay');
            if (purchasePriceInput) purchasePriceInput.value = '';
            if (currentPriceDisplay) currentPriceDisplay.textContent = 'Leave empty to use current market price';
        });
    }

    // Get current price button functionality
    const getCurrentPriceBtn = document.getElementById('getCurrentPriceBtn');
    if (getCurrentPriceBtn) {
        getCurrentPriceBtn.addEventListener('click', async function() {
            await fetchCurrentPrice();
        });
    }
}

/**
 * Prepare sell modal with stock information
 */
function prepareSell(ownedStockId, symbol, maxShares) {
    try {
        const sellOwnedStockId = document.getElementById('sellOwnedStockId');
        const sellStockSymbol = document.getElementById('sellStockSymbol');
        const sellMaxShares = document.getElementById('sellMaxShares');
        const sellQuantity = document.getElementById('sellQuantity');
        
        if (sellOwnedStockId) sellOwnedStockId.value = ownedStockId;
        if (sellStockSymbol) sellStockSymbol.textContent = symbol;
        
        // Format the number to display up to 4 decimal places, removing trailing zeros
        const formattedShares = parseFloat(maxShares).toString();
        if (sellMaxShares) sellMaxShares.textContent = formattedShares;
        if (sellQuantity) {
            sellQuantity.max = maxShares;
            sellQuantity.value = '';
        }
        
        // Store current max shares for the sell all function
        currentMaxShares = parseFloat(maxShares);
    } catch (error) {
        console.error('Error preparing sell modal:', error);
        showError('Error preparing sell form. Please try again.');
    }
}

/**
 * Sell all shares - sets the quantity input to maximum shares
 */
function sellAllShares() {
    try {
        const sellQuantity = document.getElementById('sellQuantity');
        if (sellQuantity && currentMaxShares > 0) {
            sellQuantity.value = currentMaxShares;
        } else {
            console.warn('No shares available to sell or element not found');
            showError('No shares available to sell');
        }
    } catch (error) {
        console.error('Error in sellAllShares:', error);
        showError('Error setting sell quantity. Please enter manually.');
    }
}

/**
 * Confirm stock deletion with user confirmation
 */
function confirmDeleteStock() {
    try {
        const sellStockSymbol = document.getElementById('sellStockSymbol');
        const symbol = sellStockSymbol ? sellStockSymbol.textContent : 'this stock';
        
        const confirmed = confirm(
            `⚠️ WARNING: This will permanently delete ${symbol} from your portfolio!\n\n` +
            `This action will:\n` +
            `• Sell ALL ${currentMaxShares} shares at current market price\n` +
            `• Remove the stock from your portfolio entirely\n` +
            `• Cannot be undone\n\n` +
            `Are you sure you want to continue?`
        );
        
        if (confirmed) {
            sellAllShares();
            // Auto-submit the form after a brief delay to let user see the quantity was set
            setTimeout(() => {
                const sellForm = document.querySelector('#sellStockModal form');
                if (sellForm) {
                    sellForm.submit();
                }
            }, 100);
        }
    } catch (error) {
        console.error('Error in confirmDeleteStock:', error);
        showError('Error preparing stock deletion.');
    }
}

/**
 * Fetch current stock price
 */
async function fetchCurrentPrice() {
    const symbolInput = document.getElementById('symbol');
    const purchasePriceInput = document.getElementById('purchasePrice');
    const currentPriceDisplay = document.getElementById('currentPriceDisplay');
    
    if (!symbolInput) return;
    
    const symbol = symbolInput.value.trim();
    if (!symbol) {
        showError('Please enter a stock symbol first');
        return;
    }

    try {
        const response = await fetch(`/api/stocks/price/${symbol}`);
        if (response.ok) {
            const priceData = await response.json();
            if (purchasePriceInput) {
                purchasePriceInput.value = priceData.currentPrice.toFixed(2);
            }
            if (currentPriceDisplay) {
                currentPriceDisplay.innerHTML = `Current market price: <strong>$${priceData.currentPrice.toFixed(2)}</strong>`;
            }
        } else {
            if (currentPriceDisplay) {
                currentPriceDisplay.innerHTML = `<span class="text-danger">Could not fetch current price for ${symbol}</span>`;
            }
        }
    } catch (error) {
        console.error('Error fetching stock price:', error);
        if (currentPriceDisplay) {
            currentPriceDisplay.innerHTML = '<span class="text-danger">Error fetching current price</span>';
        }
    }
}

/**
 * Connect to WebSocket for real-time updates
 */
function connectWebSocket() {
    try {
        const socket = new SockJS('/ws');
        stompClient = new StompJs.Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            debug: function (str) {
                console.log('STOMP: ' + str);
            }
        });

        stompClient.onConnect = function (frame) {
            console.log('Connected to WebSocket: ' + frame);
            updateWebSocketStatus('connected');
            
            // Subscribe to general stock price updates
            stompClient.subscribe('/topic/stock-prices', function (priceUpdate) {
                const update = JSON.parse(priceUpdate.body);
                updateStockPriceInUI(update);
            });
        };

        stompClient.onStompError = function (frame) {
            console.error('WebSocket error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
            updateWebSocketStatus('disconnected');
        };

        stompClient.activate();
    } catch (error) {
        console.error('Error connecting WebSocket:', error);
        updateWebSocketStatus('disconnected');
    }
}

/**
 * Update WebSocket connection status indicator
 */
function updateWebSocketStatus(status) {
    const indicator = document.getElementById('websocket-indicator');
    if (indicator) {
        if (status === 'connected') {
            indicator.className = 'badge websocket-connected';
            indicator.innerHTML = '<i class="bi bi-wifi"></i> Live Updates';
        } else {
            indicator.className = 'badge websocket-disconnected';
            indicator.innerHTML = '<i class="bi bi-wifi-off"></i> Disconnected';
        }
    }
}

/**
 * Update stock price in the UI
 */
function updateStockPriceInUI(priceUpdate) {
    try {
        // Update current price in portfolio table
        const portfolioRows = document.querySelectorAll('tbody tr');
        portfolioRows.forEach(row => {
            const symbolElement = row.querySelector('strong');
            if (symbolElement && symbolElement.textContent === priceUpdate.symbol) {
                // Update current price
                const currentPriceCell = row.cells[3]; // Current Price column
                if (currentPriceCell) {
                    currentPriceCell.textContent = '$' + parseFloat(priceUpdate.currentPrice).toFixed(2);
                    
                    // Add visual indicator for price change
                    currentPriceCell.classList.remove('text-success', 'text-danger', 'price-change');
                    if (priceUpdate.priceChange > 0) {
                        currentPriceCell.classList.add('text-success', 'price-change');
                    } else if (priceUpdate.priceChange < 0) {
                        currentPriceCell.classList.add('text-danger', 'price-change');
                    }
                    
                    // Update current value and profit/loss calculations
                    updateRowCalculations(row, priceUpdate.currentPrice);
                }
            }
        });

        // Update portfolio summary totals
        updatePortfolioSummary();
        
        console.log('Updated price for ' + priceUpdate.symbol + ': $' + priceUpdate.currentPrice);
    } catch (error) {
        console.error('Error updating stock price in UI:', error);
    }
}

/**
 * Update row calculations for current value and profit/loss
 */
function updateRowCalculations(row, currentPrice) {
    try {
        const quantityText = row.cells[1].textContent;
        const quantity = parseFloat(quantityText);
        const newCurrentValue = quantity * currentPrice;
        const currentValueCell = row.cells[5]; // Current Value column
        if (currentValueCell) {
            currentValueCell.textContent = '$' + newCurrentValue.toFixed(2);
        }
        
        // Update profit/loss
        const investmentText = row.cells[4].textContent.replace('$', '');
        const investment = parseFloat(investmentText);
        const profitLoss = newCurrentValue - investment;
        const profitLossPercentage = investment > 0 ? (profitLoss / investment) * 100 : 0;
        
        const profitLossCell = row.cells[6]; // Profit/Loss column
        if (profitLossCell) {
            profitLossCell.innerHTML = `
                <span class="${profitLoss >= 0 ? 'text-success' : 'text-danger'}">
                    ${profitLoss >= 0 ? '+' : '-'}$${Math.abs(profitLoss).toFixed(2)}
                    <br>
                    <small>(${profitLossPercentage.toFixed(2)}%)</small>
                </span>
            `;
        }
    } catch (error) {
        console.error('Error updating row calculations:', error);
    }
}

/**
 * Update portfolio summary totals
 */
function updatePortfolioSummary() {
    try {
        // Recalculate portfolio totals
        let totalCurrentValue = 0;
        let totalInvestment = 0;
        
        const portfolioRows = document.querySelectorAll('tbody tr');
        portfolioRows.forEach(row => {
            const currentValueText = row.cells[5].textContent.replace('$', '');
            const investmentText = row.cells[4].textContent.replace('$', '');
            totalCurrentValue += parseFloat(currentValueText) || 0;
            totalInvestment += parseFloat(investmentText) || 0;
        });
        
        // Update Current Value card
        const currentValueCard = document.querySelector('.bg-secondary .card-body h4');
        if (currentValueCard) {
            currentValueCard.textContent = '$' + totalCurrentValue.toFixed(2);
        }
        
        // Update Profit/Loss card
        const profitLoss = totalCurrentValue - totalInvestment;
        const profitLossPercentage = totalInvestment > 0 ? (profitLoss / totalInvestment) * 100 : 0;
        
        const profitLossCard = document.querySelector('.bg-success .card-body, .bg-danger .card-body');
        if (profitLossCard) {
            const card = profitLossCard.closest('.card');
            if (card) {
                card.className = 'card text-white ' + (profitLoss >= 0 ? 'bg-success' : 'bg-danger');
                
                const icon = profitLossCard.querySelector('i');
                if (icon) {
                    icon.className = 'bi display-4 mb-2 ' + (profitLoss >= 0 ? 'bi-arrow-up' : 'bi-arrow-down');
                }
                
                const valueElement = profitLossCard.querySelector('h4');
                if (valueElement) {
                    valueElement.textContent = (profitLoss >= 0 ? '+' : '-') + '$' + Math.abs(profitLoss).toFixed(2);
                }
                
                const percentageElement = profitLossCard.querySelector('small');
                if (percentageElement) {
                    percentageElement.textContent = '(' + profitLossPercentage.toFixed(2) + '%)';
                }
            }
        }
    } catch (error) {
        console.error('Error updating portfolio summary:', error);
    }
}

/**
 * Show error message to user
 */
function showError(message) {
    // Simple alert for now, could be enhanced with better UI
    console.error(message);
    alert(message);
}

/**
 * Disconnect WebSocket when page unloads
 */
window.addEventListener('beforeunload', function() {
    if (stompClient !== null) {
        stompClient.deactivate();
    }
});

// Make sellAllShares and confirmDeleteStock globally accessible for onclick handlers
window.sellAllShares = sellAllShares;
window.confirmDeleteStock = confirmDeleteStock;