import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UtcTimingService {

  constructor() { }

  /**
   * ✅ Convertit une date UTC en format local pour l'affichage
   * @param utcDateString - Date UTC au format ISO string (ex: "2025-08-15T19:00:00Z")
   * @returns Date locale formatée
   */
  formatUtcToLocal(utcDateString: string): Date {
    if (!utcDateString) return new Date();
    
    // Gérer différents formats de dates UTC
    let dateToParse = utcDateString;
    
    // Si la date n'a pas de timezone, on l'assume UTC
    if (!utcDateString.includes('Z') && !utcDateString.includes('+') && !utcDateString.includes('-', 10)) {
      // Date au format "2025-08-15T19:00:00" - on l'assume UTC
      dateToParse = utcDateString + 'Z';
    }
    
    // Parser la date UTC
    const utcDate = new Date(dateToParse);
    
    // Vérifier que la date est valide
    if (isNaN(utcDate.getTime())) {
      console.warn('Invalid UTC date string:', utcDateString);
      return new Date();
    }
    
    return utcDate;
  }

  /**
   * ✅ Formate une date UTC pour l'affichage dans l'interface utilisateur
   * @param utcDateString - Date UTC au format ISO string
   * @param options - Options de formatage
   * @returns String formatée
   */
  formatUtcForDisplay(utcDateString: string, options?: {
    includeTime?: boolean;
    includeSeconds?: boolean;
    locale?: string;
  }): string {
    if (!utcDateString) return '';

    const date = this.formatUtcToLocal(utcDateString);
    const defaultOptions = {
      includeTime: true,
      includeSeconds: false,
      locale: 'fr-FR',
      ...options
    };

    const formatOptions: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      ...(defaultOptions.includeTime && {
        hour: '2-digit',
        minute: '2-digit',
        ...(defaultOptions.includeSeconds && { second: '2-digit' })
      })
    };

    return date.toLocaleDateString(defaultOptions.locale, formatOptions);
  }

  /**
   * ✅ Formate une date UTC pour les inputs datetime-local
   * @param utcDateString - Date UTC au format ISO string
   * @returns String au format YYYY-MM-DDTHH:mm
   */
  formatUtcForInput(utcDateString: string): string {
    if (!utcDateString) return '';

    const date = this.formatUtcToLocal(utcDateString);
    return date.toISOString().slice(0, 16);
  }

  /**
   * ✅ Convertit une date locale en UTC pour l'envoi au backend
   * @param localDateString - Date locale au format YYYY-MM-DDTHH:mm
   * @returns String UTC au format ISO
   */
  convertLocalToUtc(localDateString: string): string {
    if (!localDateString) return '';

    // Créer une date locale et la convertir en UTC
    const localDate = new Date(localDateString);
    return localDate.toISOString();
  }

  /**
   * ✅ Calcule le temps restant jusqu'à une date UTC
   * @param utcDateString - Date UTC cible
   * @returns String formatée du temps restant
   */
  getTimeRemaining(utcDateString: string): string {
    if (!utcDateString) return '';

    const now = new Date();
    const target = this.formatUtcToLocal(utcDateString);
    const diff = target.getTime() - now.getTime();

    if (diff <= 0) {
      return 'Deadline Passed';
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((diff / (1000 * 60)) % 60);

    if (days > 0) {
      return `${days}j ${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h ${minutes}m`;
    } else {
      return `${minutes}m`;
    }
  }

  /**
   * ✅ Vérifie si une date UTC est dans le passé
   * @param utcDateString - Date UTC à vérifier
   * @returns boolean
   */
  isPast(utcDateString: string): boolean {
    if (!utcDateString) return false;
    
    const target = this.formatUtcToLocal(utcDateString);
    return target < new Date();
  }

  /**
   * ✅ Vérifie si une date UTC est dans le futur
   * @param utcDateString - Date UTC à vérifier
   * @returns boolean
   */
  isFuture(utcDateString: string): boolean {
    if (!utcDateString) return false;
    
    const target = this.formatUtcToLocal(utcDateString);
    return target > new Date();
  }

  /**
   * ✅ Vérifie si une date UTC est aujourd'hui
   * @param utcDateString - Date UTC à vérifier
   * @returns boolean
   */
  isToday(utcDateString: string): boolean {
    if (!utcDateString) return false;
    
    const target = this.formatUtcToLocal(utcDateString);
    const today = new Date();
    
    return target.toDateString() === today.toDateString();
  }

  /**
   * ✅ Obtient le statut d'une gameweek basé sur les dates UTC
   * @param startDate - Date de début UTC
   * @param endDate - Date de fin UTC
   * @param joinDeadline - Date limite d'inscription UTC
   * @returns string - Statut de la gameweek
   */
  getGameweekStatus(startDate: string, endDate: string, joinDeadline: string, matchDates?: string[]): string {
    const now = new Date();
    let firstMatch: Date | null = null;
    let lastMatch: Date | null = null;
    if (Array.isArray(matchDates) && matchDates.length > 0) {
      const parsed = matchDates.map(d => this.formatUtcToLocal(d)).filter(d => !isNaN(d.getTime()));
      if (parsed.length > 0) {
        firstMatch = new Date(Math.min(...parsed.map(d => d.getTime())));
        lastMatch = new Date(Math.max(...parsed.map(d => d.getTime())));
      }
    }
    // Fallback to start/end if no matchDates provided
    const start = firstMatch || this.formatUtcToLocal(startDate);
    const end = lastMatch || this.formatUtcToLocal(endDate);
    if (now < start) {
      console.log('Status: UPCOMING');
      return 'UPCOMING';
    } else if (now >= start && now <= end) {
      console.log('Status: ONGOING');
      return 'ONGOING';
    } else {
      console.log('Status: FINISHED');
      return 'FINISHED';
    }
  }

  /**
   * ✅ Vérifie si l'inscription est encore possible
   * @param joinDeadline - Date limite d'inscription UTC
   * @returns boolean
   */
  canStillJoin(joinDeadline: string): boolean {
    if (!joinDeadline) return false;
    
    const deadline = this.formatUtcToLocal(joinDeadline);
    return new Date() < deadline;
  }

  /**
   * ✅ Obtient la timezone de l'utilisateur
   * @returns string - Timezone de l'utilisateur
   */
  getUserTimezone(): string {
    return Intl.DateTimeFormat().resolvedOptions().timeZone;
  }

  /**
   * ✅ Formate une date avec indication de timezone
   * @param utcDateString - Date UTC
   * @returns string - Date formatée avec timezone
   */
  formatWithTimezone(utcDateString: string): string {
    if (!utcDateString) return '';

    const date = this.formatUtcToLocal(utcDateString);
    const timezone = this.getUserTimezone();
    
    return date.toLocaleString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      timeZoneName: 'short'
    }) + ` (${timezone})`;
  }
}
