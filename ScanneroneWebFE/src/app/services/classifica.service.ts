import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, of } from 'rxjs';
import { LeaderboardEntryDto } from '../dto/leaderboard-entry.dto';
import { UserProfileDto } from '../dto/user-profile.dto';

@Injectable({
  providedIn: 'root'
})
export class ClassificaService {
  private baseUrl = '/api';
  constructor(private http: HttpClient) {}

  /**
   * Restituisce la classifica in base alla categoria selezionata
   */
  getLeaderboard(category: string, limit: number = 50, offset: number = 0, country?: string, region?: string): Observable<LeaderboardEntryDto[]> {
    let params = new HttpParams()
      .set('limit', limit)
      .set('offset', offset);
    
    if (country) params = params.set('country', country);
    if (region) params = params.set('region', region);

    let endpoint = '/api/leaderboard/global';
    
    if (category === 'discovery') {
      endpoint = '/api/leaderboard/discovery';
    } else if (category === 'travelers') {
      endpoint = '/api/leaderboard/travelers';
    }

    return this.http.get<LeaderboardEntryDto[]>(endpoint, { params }).pipe(
      catchError(() => of([]))
    );
  }

  getUserRank(userId: number): Observable<number> {
    return this.http.get<number>(`/api/users/${userId}/rank`).pipe(
      catchError(() => of(0))
    );
  }

  getUserProfile(username: string): Observable<UserProfileDto> {
    return this.http.get<any>(`/api/stats/user/${username}`).pipe(
        map((stats: any) => ({
            username: stats.username || username,
            avatar: (stats.username || username).substring(0, 2).toUpperCase(),
            joinDate: 'Gennaio 2025',
            location: 'Italia',
            score: (stats.score || 0).toLocaleString(),
            globalRank: stats.rank || 0,
            reti: {
                totali: stats.totalUploaded || 0,
                uniche: stats.uniqueDiscovered || 0,
                accuracy: stats.avgAccuracy ? stats.avgAccuracy.toFixed(1) + ' m' : 'N/A',
                indirizzoCompleto: ((stats.geoCompletionPercent || 0) * 100).toFixed(0) + '%'
            },
            geografia: {
                paesi: stats.countriesCovered || 0,
                regioni: 0,
                citta: stats.citiesCovered || 0,
                nuoveCitta: 0
            }
        })),
        catchError(() => of({
            username: username,
            avatar: '??',
            joinDate: 'N/A',
            location: 'Sconosciuta',
            score: '0',
            globalRank: 0,
            reti: { totali: 0, uniche: 0, accuracy: 'N/A', indirizzoCompleto: '0%' },
            geografia: { paesi: 0, regioni: 0, citta: 0, nuoveCitta: 0 }
        }))
    );
  }
}
