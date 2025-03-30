<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Notification extends Model
{
    use HasFactory;

    protected $table = 'desafio';

    protected $fillable = [
        'title',
        'message',
        'image',
        'time',
    ];
    
    // 'Time' debe tratarse como objeto datetime
    protected $casts = [
        'time' => 'datetime',
    ];
}
